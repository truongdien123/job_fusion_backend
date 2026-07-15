package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.exceptions.*;
import com.tma.job_fusion_backend.models.*;
import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.repositories.*;
import com.tma.job_fusion_backend.repositories.query.TenantQueryRepository;
import com.tma.job_fusion_backend.repositories.query.UserRoleQueryRepository;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.services.TenantService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.JwtUtil;
import com.tma.job_fusion_backend.utils.PasswordUtil;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import com.tma.job_fusion_backend.mappers.TenantMapper;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tma.job_fusion_backend.components.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    @Value("${app.activation}")
    private String activationLink;

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final TenantMapper tenantMapper;
    private final TenantQueryRepository tenantQueryRepository;
    private final UserRoleQueryRepository userRoleQueryRepository;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new BadRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAN_NOT_FOUND));

        Tenant tenant = tenantMapper.toEntity(request);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setPlan(plan);
        tenant.setCreatedBy(jwtUtil.getCurrentUserId());
        tenant.setExpirationDate(DateTimeUtil.nowUtc().plusMonths(1));
        tenant.setCompanySize(1);

        Tenant savedTenant = tenantRepository.save(tenant);

        String generatedPassword = PasswordUtil.generateRandomPassword();

        User adminUser = new User();
        adminUser.setEmail(request.getAdminEmail());
        adminUser.setPassword(passwordEncoder.encode(generatedPassword));
        adminUser.setFullName(request.getAdminFullName());
        adminUser.setStatus(UserStatus.PENDING);
        adminUser.setType(UserType.TENANT);
        adminUser.setTenant(savedTenant);
        adminUser.setCreatedBy(jwtUtil.getCurrentUserId());

        User savedAdminUser = userRepository.save(adminUser);

        Role tenantAdminRole = roleRepository.findByName(RoleConstant.TENANT_ADMIN)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));

        UserRole userRole = new UserRole();
        userRole.setUser(savedAdminUser);
        userRole.setRole(tenantAdminRole);
        userRole.setCreatedBy(jwtUtil.getCurrentUserId());
        userRoleRepository.save(userRole);

        String activationTokenStr = UUID.randomUUID().toString();
        UserToken activationToken = new UserToken();
        activationToken.setUser(savedAdminUser);
        activationToken.setToken(activationTokenStr);
        activationToken.setTokenType(TokenType.ACTIVATION);
        activationToken.setExpiredAt(DateTimeUtil.nowUtc().plusDays(7));
        activationToken.setUsed(false);
        userTokenRepository.save(activationToken);

        emailService.sendTenantCreatedEmail(
                TenantCreatedEmailDto.builder()
                        .toEmail(savedAdminUser.getEmail())
                        .adminName(savedAdminUser.getFullName())
                        .tenantName(savedTenant.getCompanyName())
                        .dashboardImageUrl(null)
                        .adminPassword(generatedPassword)
                        .role(RoleConstant.TENANT_ADMIN)
                        .activationUrl(activationLink)
                        .build()
        );

        return tenantMapper.toTenantResponse(savedTenant, savedAdminUser.getId(), 1L);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> getListTenant(Pageable pageable) {
        return tenantQueryRepository.findAllActiveTenants(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantDetail(UUID id) {
        Tenant tenant = findTenantById(id);

        UUID adminUserId = getAdminUserId(tenant.getId());

        long activeUsers = userRepository.countByTenantIdAndDeletedAtIsNull(tenant.getId());
        return tenantMapper.toTenantResponse(tenant, adminUserId, activeUsers);
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(UUID id, TenantRequest request) {
        Tenant tenant = findTenantById(id);

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        boolean isSuperAdmin = RoleConstant.SUPER_ADMIN.equalsIgnoreCase(currentUser.getRole());
        if (!isSuperAdmin) {
            if (ObjectUtils.isEmpty(currentUser.getTenantId()) || !currentUser.getTenantId().equals(id)) {
                throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
            }
            if (ObjectUtils.isNotEmpty(request.getStatus()) && request.getStatus() != tenant.getStatus()) {
                throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
            }
            if (ObjectUtils.isNotEmpty(request.getPlanId()) && (ObjectUtils.isEmpty(tenant.getPlan()) || !request.getPlanId().equals(tenant.getPlan().getId()))) {
                throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
            }
        }

        validationUtil.validateAndSetPlan(tenant, request.getPlanId());

        if (ObjectUtils.isNotEmpty(request.getStatus())) {
            tenant.setStatus(request.getStatus());
        }

        tenantMapper.updateTenant(request, tenant);
        tenant.setUpdatedBy(currentUser.getId());

        Tenant savedTenant = tenantRepository.save(tenant);

        UUID adminUserId = getAdminUserId(savedTenant.getId());

        long activeUsers = userRepository.countByTenantIdAndDeletedAtIsNull(savedTenant.getId());
        return tenantMapper.toTenantResponse(savedTenant, adminUserId, activeUsers);
    }

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        Tenant tenant = findTenantById(id);

        UserPrincipal currentUser = jwtUtil.getCurrentUser();
        UUID currentUserId = ObjectUtils.isNotEmpty(currentUser) ? currentUser.getId() : null;

        LocalDateTime now = DateTimeUtil.nowUtc();
        tenant.setDeletedAt(now);
        tenant.setStatus(TenantStatus.INACTIVE);
        tenant.setUpdatedBy(currentUserId);
        tenantRepository.save(tenant);

        List<User> tenantUsers = userRepository.findAllByTenantIdAndDeletedAtIsNull(id);
        for (User user : tenantUsers) {
            user.setDeletedAt(now);
            user.setStatus(UserStatus.DISABLED);
            user.setUpdatedBy(currentUserId);
        }
        userRepository.saveAll(tenantUsers);
    }

    private UUID getAdminUserId(UUID tenantId) {
        return userRoleQueryRepository.findTenantAdminByTenantId(tenantId)
                .map(User::getId)
                .orElse(null);
    }

    private Tenant findTenantById(UUID id) {
        return tenantQueryRepository.findTenantWithDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TENANT_NOT_FOUND));
    }
}
