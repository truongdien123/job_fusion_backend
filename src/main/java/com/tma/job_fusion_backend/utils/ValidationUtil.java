package com.tma.job_fusion_backend.utils;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.BillingCycle;
import com.tma.job_fusion_backend.exceptions.NotActiveException;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.repositories.PlanRepository;
import com.tma.job_fusion_backend.repositories.TenantRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ValidationUtil {

    private final JwtUtil jwtUtil;
    private final PlanRepository planRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public UserPrincipal getRequiredCurrentUser() {
        UserPrincipal currentUser = jwtUtil.getCurrentUser();
        if (ObjectUtils.isEmpty(currentUser)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
        validateUserAndTenantActive(currentUser);
        return currentUser;
    }

    public void validateUserAndTenantActive(UserPrincipal principal) {
        if (ObjectUtils.isEmpty(principal)) {
            return;
        }

        if (principal.getTenantId() != null && !principal.hasRole(RoleConstant.SUPER_ADMIN)) {
            Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(principal.getTenantId()).orElse(null);
            if (ObjectUtils.isEmpty(tenant) || tenant.getStatus() != TenantStatus.ACTIVE || tenant.getDeletedAt() != null) {
                throw new NotActiveException(ErrorCode.TENANT_INACTIVE);
            }
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(principal.getId()).orElse(null);
        if (ObjectUtils.isEmpty(user) || user.getStatus() != UserStatus.ACTIVE || user.getDeletedAt() != null) {
            throw new NotActiveException(ErrorCode.INACTIVE_USER);
        }
    }

    public void validateAndSetPlan(Tenant tenant, UUID planId) {
        if (ObjectUtils.isNotEmpty(planId) && 
            (ObjectUtils.isEmpty(tenant.getPlan()) || !planId.equals(tenant.getPlan().getId()))) {
            
            Plan plan = planRepository.findByIdAndDeletedAtIsNull(planId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PLAN_NOT_FOUND));
            tenant.setPlan(plan);
            tenant.setMaxStaffAccount(plan.getMaxStaffAccount());
            tenant.setMaxActiveJobPosting(plan.getMaxActiveJobPosting());
            tenant.setPrice(plan.getPrice());
            tenant.setBillingCycle(plan.getBillingCycle());
            tenant.setFeature(plan.getFeature());
            tenant.setExpirationDate(plan.getBillingCycle() == BillingCycle.YEARLY
                    ? DateTimeUtil.nowUtc().plusDays(365)
                    : (plan.getBillingCycle() == BillingCycle.SIX_MONTHLY
                        ? DateTimeUtil.nowUtc().plusDays(180)
                        : DateTimeUtil.nowUtc().plusDays(30)));
        }
    }
}

