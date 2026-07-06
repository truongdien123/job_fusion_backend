package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.exceptions.*;
import com.tma.job_fusion_backend.models.*;
import com.tma.job_fusion_backend.pojo.requests.CreateTenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.repositories.*;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.services.TenantService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.JwtUtil;
import com.tma.job_fusion_backend.utils.PasswordUtil;
import com.tma.job_fusion_backend.mappers.TenantMapper;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    @Value("${app.loginUrl}")
    private String loginUrl;

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new EmailAlreadyExistsException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException(ErrorCode.PLAN_NOT_FOUND));

        Tenant tenant = tenantMapper.toEntity(request);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setCompanySize(plan.getMaxStaffAccount().toString());
        tenant.setPlan(plan);
        tenant.setCreatedBy(jwtUtil.getCurrentUserId());

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
        adminUser.setCreatedBy(jwtUtil.getCurrentUserId());

        User savedAdminUser = userRepository.save(adminUser);

        Role tenantAdminRole = roleRepository.findByName("Tenant Admin")
                .orElseThrow(() -> new RoleNotFoundException(ErrorCode.ROLE_NOT_FOUND));

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
                        .loginUrl(loginUrl)
                        .dashboardImageUrl(null)
                        .adminPassword(generatedPassword)
                        .build()
        );

        return tenantMapper.toTenantResponse(savedTenant, savedAdminUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> getListTenant(Pageable pageable) {
        return tenantRepository.findAll(pageable).map(tenant -> {
            UUID adminUserId = userRoleRepository.findTenantAdminByTenantId(tenant.getId())
                    .map(User::getId)
                    .orElse(null);
            return tenantMapper.toTenantResponse(tenant, adminUserId);
        });
    }
}
