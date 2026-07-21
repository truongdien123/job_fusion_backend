package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.mappers.UserMapper;
import com.tma.job_fusion_backend.models.*;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.StaffRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.repositories.*;
import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.repositories.query.UserQueryRepository;
import com.tma.job_fusion_backend.repositories.query.UserTokenQueryRepository;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.services.UserService;
import com.tma.job_fusion_backend.utils.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserMapper userMapper;
    private final ValidationUtil validationUtil;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final UserTokenRepository userTokenRepository;
    private final UserTokenQueryRepository userTokenQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserDetail(UUID id) {
        User user = findUserById(id);

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UserUtil.validateAccess(id, user, currentUser);

        String resolvedRole = UserUtil.resolveUserRole(user, userRoleRepository);
        UserResponse response = userMapper.toUserResponse(user);
        response.setUserRole(resolvedRole);
        return response;
    }

    @Override
    @Transactional
    public UserResponse createStaff(StaffRequest request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = getRequiredTenantId(currentUser);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TENANT_NOT_FOUND));

        // Validate plan max staff account limit
        if (tenant.getPlan() != null && tenant.getPlan().getMaxStaffAccount() != null) {
            long currentStaffCount = userRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
            if (currentStaffCount >= tenant.getPlan().getMaxStaffAccount()) {
                throw new BadRequestException(ErrorCode.MAX_STAFF_LIMIT_REACHED);
            }
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        String password = PasswordUtil.generateRandomPassword();

        // Map request to User entity
        User staff = userMapper.toEntity(request);
        staff.setPassword(passwordEncoder.encode(password));
        staff.setStatus(UserStatus.PENDING);
        staff.setType(UserType.TENANT);
        staff.setTenant(tenant);
        staff.setActivatedDate(DateTimeUtil.nowUtc());
        staff.setRequirePasswordChange(true);
        staff.setCreatedBy(currentUser.getId());
        staff.setEmployeeCode(UserUtil.generateEmployeeCode());

        String token = UserUtil.createAndSaveUserRole(staff, jwtUtil, userTokenRepository);

        User savedStaff = userRepository.save(staff);

        // Find and assign requested roles
        assignRolesToUser(savedStaff, request.getRole(), currentUser.getId(), false);

        emailService.sendTenantCreatedEmail(
                TenantCreatedEmailDto.builder()
                        .toEmail(savedStaff.getEmail())
                        .adminName(savedStaff.getFullName())
                        .tenantName(tenant.getCompanyName())
                        .activationUrl(jwtUtil.getActivationUrl(token))
                        .dashboardImageUrl(null)
                        .adminPassword(password)
                        .role(String.join(", ", request.getRole()))
                        .build()
        );

        UserResponse response = userMapper.toUserResponse(savedStaff);
        response.setUserRole(String.join(", ", request.getRole()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getListStaff(PagingRequest<?> request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = getRequiredTenantId(currentUser);

        Page<User> staffPage = userQueryRepository.findStaffByTenantId(tenantId, RoleConstant.TENANT_ADMIN, request.toPageable());
        Page<UserResponse> mappedPage = staffPage.map(user -> {
            UserResponse response = userMapper.toUserResponse(user);
            response.setUserRole(UserUtil.resolveUserRole(user, userRoleRepository));
            return response;
        });

        return PageResponse.of(mappedPage);
    }

    @Override
    @Transactional
    public UserResponse updateStaff(UUID id, StaffRequest request) {
        User staff = findUserById(id);

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        validateTenantAdminAccess(currentUser, staff);

        staff.setFullName(request.getFullName());
        if (request.getStatus() != null) {
            staff.setStatus(request.getStatus());
        }
        staff.setUpdatedBy(currentUser.getId());

        User savedStaff = userRepository.save(staff);

        // Find and assign requested roles
        assignRolesToUser(savedStaff, request.getRole(), currentUser.getId(), true);

        UserResponse response = userMapper.toUserResponse(savedStaff);
        response.setUserRole(String.join(", ", request.getRole()));

        return response;
    }

    @Override
    @Transactional
    public void deleteStaff(UUID id) {
        User staff = findUserById(id);

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        validateTenantAdminAccess(currentUser, staff);

        // Soft delete user
        staff.setDeletedAt(DateTimeUtil.nowUtc());
        staff.setUpdatedBy(currentUser.getId());
        userRepository.save(staff);

        // Soft delete user roles
        List<UserRole> userRoles = userRoleRepository.findByUser(staff);
        for (UserRole ur : userRoles) {
            ur.setDeletedAt(DateTimeUtil.nowUtc());
            ur.setUpdatedBy(currentUser.getId());
            userRoleRepository.save(ur);
        }
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private UUID getRequiredTenantId(UserPrincipal currentUser) {
        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
        return tenantId;
    }

    private void validateTenantAdminAccess(UserPrincipal currentUser, User staff) {
        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN) || 
            staff.getTenant() == null ||
            !currentUser.getTenantId().equals(staff.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void assignRolesToUser(User user, List<String> roleNames, UUID currentUserId, boolean clearExisting) {
        List<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new NotFoundException(ErrorCode.ROLE_NOT_FOUND);
        }

        if (clearExisting) {
            userRoleRepository.deleteByUser(user);
        }

        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setCreatedBy(currentUserId);
            userRoleRepository.save(userRole);
        }
    }

    @Override
    @Transactional
    public void resendStaffActivation(UUID id) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = getRequiredTenantId(currentUser);

        User staff = findUserById(id);

        // Access control check: Target user must belong to the same tenant
        if (ObjectUtils.isEmpty(staff.getTenant()) || !tenantId.equals(staff.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        // Validate status: must be PENDING
        if (staff.getStatus() != UserStatus.PENDING) {
            throw new BadRequestException(ErrorCode.STAFF_ALREADY_ACTIVE);
        }

        // Invalidate old tokens
        userTokenQueryRepository.invalidateOldToken(staff, TokenType.ACTIVATION, DateTimeUtil.nowUtc());

        // Generate new password
        String newPassword = PasswordUtil.generateRandomPassword();
        staff.setPassword(passwordEncoder.encode(newPassword));
        staff.setRequirePasswordChange(true);
        userRepository.save(staff);

        // Generate new activation token
        String token = UserUtil.createAndSaveUserRole(staff, jwtUtil, userTokenRepository);

        // Fetch user roles
        String resolvedRole = UserUtil.resolveUserRole(staff, userRoleRepository);

        // Send email
        emailService.sendTenantCreatedEmail(
                TenantCreatedEmailDto.builder()
                        .toEmail(staff.getEmail())
                        .adminName(staff.getFullName())
                        .tenantName(staff.getTenant().getCompanyName())
                        .activationUrl(jwtUtil.getActivationUrl(token))
                        .dashboardImageUrl(null)
                        .adminPassword(newPassword)
                        .role(resolvedRole)
                        .build()
        );
    }
}
