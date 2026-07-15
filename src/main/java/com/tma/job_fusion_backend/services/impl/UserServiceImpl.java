package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.mappers.UserMapper;
import com.tma.job_fusion_backend.models.Role;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserRole;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.StaffRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.repositories.RoleRepository;
import com.tma.job_fusion_backend.repositories.TenantRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.repositories.UserRoleRepository;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.services.UserService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import org.springframework.beans.factory.annotation.Value;
import com.tma.job_fusion_backend.utils.PasswordUtil;
import com.tma.job_fusion_backend.utils.UserUtil;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final ValidationUtil validationUtil;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.loginUrl}")
    private String loginUrl;

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

        // Ensure the current user is Tenant Admin
        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

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
        staff.setStatus(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE);
        staff.setType(UserType.TENANT);
        staff.setTenant(tenant);
        staff.setActivatedDate(DateTimeUtil.nowUtc());
        staff.setCreatedBy(currentUser.getId());
        staff.setEmployeeCode(UserUtil.generateEmployeeCode());

        User savedStaff = userRepository.save(staff);

        // Find and assign requested roles
        List<Role> roles = roleRepository.findByNameIn(request.getRole());
        if (roles.size() != request.getRole().size()) {
            throw new NotFoundException(ErrorCode.ROLE_NOT_FOUND);
        }

        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(savedStaff);
            userRole.setRole(role);
            userRole.setCreatedBy(currentUser.getId());
            userRoleRepository.save(userRole);
        }

        emailService.sendTenantCreatedEmail(
                TenantCreatedEmailDto.builder()
                        .toEmail(savedStaff.getEmail())
                        .adminName(savedStaff.getFullName())
                        .tenantName(tenant.getCompanyName())
                        .loginUrl(loginUrl)
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

        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        Page<User> staffPage = userRepository.findAllByTenantIdAndDeletedAtIsNull(tenantId, request.toPageable());
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
        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN) || 
            staff.getTenant() == null ||
            !currentUser.getTenantId().equals(staff.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        staff.setFullName(request.getFullName());
        if (request.getStatus() != null) {
            staff.setStatus(request.getStatus());
        }
        staff.setUpdatedBy(currentUser.getId());

        User savedStaff = userRepository.save(staff);

        // Find and assign requested roles
        List<Role> roles = roleRepository.findByNameIn(request.getRole());
        if (roles.size() != request.getRole().size()) {
            throw new NotFoundException(ErrorCode.ROLE_NOT_FOUND);
        }

        // Delete existing roles and assign new ones
        userRoleRepository.deleteByUser(savedStaff);

        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(savedStaff);
            userRole.setRole(role);
            userRole.setCreatedBy(currentUser.getId());
            userRoleRepository.save(userRole);
        }

        UserResponse response = userMapper.toUserResponse(savedStaff);
        response.setUserRole(String.join(", ", request.getRole()));

        return response;
    }

    private User findUserById(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteStaff(UUID id) {
        User staff = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN) || 
            staff.getTenant() == null ||
            !currentUser.getTenantId().equals(staff.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

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
}
