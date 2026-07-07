package com.tma.job_fusion_backend.utils;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.repositories.PlanRepository;
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

    public UserPrincipal getRequiredCurrentUser() {
        UserPrincipal currentUser = jwtUtil.getCurrentUser();
        if (ObjectUtils.isEmpty(currentUser)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
        return currentUser;
    }

    public void validateAndSetPlan(Tenant tenant, UUID planId) {
        if (ObjectUtils.isNotEmpty(planId) && 
            (ObjectUtils.isEmpty(tenant.getPlan()) || !planId.equals(tenant.getPlan().getId()))) {
            
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PLAN_NOT_FOUND));
            tenant.setPlan(plan);
        }
    }
}
