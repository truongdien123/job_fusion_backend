package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.enums.*;
import com.tma.job_fusion_backend.exceptions.*;
import com.tma.job_fusion_backend.models.*;
import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.dtos.TenantFilter;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.repositories.*;
import com.tma.job_fusion_backend.repositories.query.TenantQueryRepository;
import com.tma.job_fusion_backend.repositories.query.UserQueryRepository;
import com.tma.job_fusion_backend.repositories.query.UserRoleQueryRepository;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.services.TenantService;
import com.tma.job_fusion_backend.utils.*;
import com.tma.job_fusion_backend.mappers.TenantMapper;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Value("${app.login}")
    private String loginLink;

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
    private final UserQueryRepository userQueryRepository;
    private final UserRoleQueryRepository userRoleQueryRepository;
    private final ValidationUtil validationUtil;
    private final JobPostingRepository jobPostingRepository;

    @Override
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getAdminEmail())) {
            throw new BadRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        validateCompanyNameUniqueness(request.getCompanyName(), null);
        validateDomainUniqueness(request.getDomain(), null);

        Plan plan = planRepository.findByIdAndDeletedAtIsNull(request.getPlanId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.PLAN_NOT_FOUND));

        Tenant tenant = tenantMapper.toEntity(request);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setPlan(plan);
        tenant.setMaxStaffAccount(plan.getMaxStaffAccount());
        tenant.setMaxActiveJobPosting(plan.getMaxActiveJobPosting());
        tenant.setPrice(plan.getPrice());
        tenant.setBillingCycle(plan.getBillingCycle());
        tenant.setFeature(plan.getFeature());
        tenant.setCreatedBy(jwtUtil.getCurrentUserId());
        tenant.setExpirationDate(plan.getBillingCycle() == BillingCycle.YEARLY
                ? DateTimeUtil.nowUtc().plusDays(365)
                : (plan.getBillingCycle() == BillingCycle.SIX_MONTHLY
                    ? DateTimeUtil.nowUtc().plusDays(180)
                    : DateTimeUtil.nowUtc().plusDays(30)));
        tenant.setCompanySize(0);

        Tenant savedTenant = tenantRepository.save(tenant);

        String generatedPassword = PasswordUtil.generateRandomPassword();

        User adminUser = new User();
        adminUser.setEmail(request.getAdminEmail());
        adminUser.setPassword(passwordEncoder.encode(generatedPassword));
        adminUser.setFullName(request.getAdminFullName());
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setType(UserType.TENANT);
        adminUser.setTenant(savedTenant);
        adminUser.setActivatedDate(DateTimeUtil.nowUtc());
        adminUser.setRequirePasswordChange(true);
        adminUser.setCreatedBy(jwtUtil.getCurrentUserId());

        User savedAdminUser = userRepository.save(adminUser);

        Role tenantAdminRole = roleRepository.findByName(RoleConstant.TENANT_ADMIN)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ROLE_NOT_FOUND));

        UserRole userRole = new UserRole();
        userRole.setUser(savedAdminUser);
        userRole.setRole(tenantAdminRole);
        userRole.setCreatedBy(jwtUtil.getCurrentUserId());
        userRoleRepository.save(userRole);

        emailService.sendTenantCreatedEmail(
                TenantCreatedEmailDto.builder()
                        .toEmail(savedAdminUser.getEmail())
                        .adminName(savedAdminUser.getFullName())
                        .tenantName(savedTenant.getCompanyName())
                        .adminPassword(generatedPassword)
                        .role(RoleConstant.TENANT_ADMIN)
                        .loginUrl(loginLink)
                        .build()
        );

        return tenantMapper.toResponse(savedTenant, savedAdminUser.getId(), 0L, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> getListTenant(TenantFilter filter, Pageable pageable) {
        return tenantQueryRepository.findAllActiveTenants(filter, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantDetail(UUID id) {
        Tenant tenant = findTenantById(id);

        UUID adminUserId = getAdminUserId(tenant.getId());

        long activeUsers = userQueryRepository.countStaffByTenantId(tenant.getId(), RoleConstant.TENANT_ADMIN);
        long activeJob = jobPostingRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenant.getId(), JobStatus.OPEN);
        return tenantMapper.toResponse(tenant, adminUserId, activeUsers, activeJob);
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

        if (StringUtils.isNotEmpty(request.getCompanyName()) && 
            !request.getCompanyName().trim().equalsIgnoreCase(tenant.getCompanyName())) {
            validateCompanyNameUniqueness(request.getCompanyName(), id);
        }

        if (StringUtils.isNotEmpty(request.getDomain()) && 
            !request.getDomain().trim().equalsIgnoreCase(tenant.getDomain())) {
            validateDomainUniqueness(request.getDomain(), id);
        }

        validationUtil.validateAndSetPlan(tenant, request.getPlanId());

        if (ObjectUtils.isNotEmpty(request.getStatus())) {
            tenant.setStatus(request.getStatus());
        }

        tenantMapper.updateEntity(request, tenant);
        tenant.setUpdatedBy(currentUser.getId());

        Tenant savedTenant = tenantRepository.save(tenant);

        UUID adminUserId = getAdminUserId(savedTenant.getId());

        long activeUsers = userQueryRepository.countStaffByTenantId(savedTenant.getId(), RoleConstant.TENANT_ADMIN);
        long activeJob = jobPostingRepository.countByTenantIdAndStatusAndDeletedAtIsNull(savedTenant.getId(), JobStatus.OPEN);
        return tenantMapper.toResponse(savedTenant, adminUserId, activeUsers, activeJob);
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

    private void validateCompanyNameUniqueness(String companyName, UUID excludeId) {
        if (StringUtils.isEmpty(companyName)) {
            return;
        }
        String trimmedName = companyName.trim();
        boolean exists = (ObjectUtils.isEmpty(excludeId))
                ? tenantRepository.existsByCompanyNameIgnoreCaseAndDeletedAtIsNull(trimmedName)
                : tenantRepository.existsByCompanyNameIgnoreCaseAndIdNotAndDeletedAtIsNull(trimmedName, excludeId);

        if (exists) {
            throw new BadRequestException(ErrorCode.COMPANY_NAME_ALREADY_EXISTS);
        }
    }

    private void validateDomainUniqueness(String domain, UUID excludeId) {
        if (StringUtils.isEmpty(domain)) {
            return;
        }
        String trimmedDomain = domain.trim();
        boolean exists = (ObjectUtils.isEmpty(excludeId))
                ? tenantRepository.existsByDomainIgnoreCaseAndDeletedAtIsNull(trimmedDomain)
                : tenantRepository.existsByDomainIgnoreCaseAndIdNotAndDeletedAtIsNull(trimmedDomain, excludeId);

        if (exists) {
            throw new BadRequestException(ErrorCode.DOMAIN_ALREADY_EXISTS);
        }
    }
}
