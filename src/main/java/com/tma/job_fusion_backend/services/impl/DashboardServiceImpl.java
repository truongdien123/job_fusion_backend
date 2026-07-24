package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsTenantResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsPlanResponse;
import com.tma.job_fusion_backend.repositories.query.TenantQueryRepository;
import com.tma.job_fusion_backend.repositories.query.PlanQueryRepository;
import com.tma.job_fusion_backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TenantQueryRepository tenantQueryRepository;
    private final PlanQueryRepository planQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsTenantResponse getDashboardStatsTenant() {
        // 1. Calculate total cumulative revenue from active tenants
        Double totalRevenue = tenantQueryRepository.calculateTotalRevenue();
        // 2. Count the number of active tenants in the system (status = ACTIVE)
        Long activeTenants = tenantQueryRepository.countActiveTenants();
        // 3. Calculate the average usage efficiency of accounts (Staff accounts / Max staff accounts)
        Double averageUsage = tenantQueryRepository.calculateAverageUsage();
        // 4. Calculate the churn rate for the current quarter
        Double churnRate = tenantQueryRepository.calculateChurnRate();

        return DashboardStatsTenantResponse.builder()
                .totalRevenue(Objects.requireNonNullElse(totalRevenue, 0.0))
                .activeTenants(Objects.requireNonNullElse(activeTenants, 0L))
                .averageUsage(Objects.requireNonNullElse(averageUsage, 0.0))
                .churnRate(Objects.requireNonNullElse(churnRate, 0.0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsPlanResponse getDashboardStatsPlan() {
        // 1. Count the number of active subscription plans
        Long activePlans = planQueryRepository.countActivePlans();
        // 2. Count the number of new plans created in the current month
        Long activePlansTrend = planQueryRepository.countActivePlansCreatedThisMonth();

        // 3. Find the top tier plan (the most subscribed active plan)
        String topTierPlanName = "";
        Long topTierSubscribers = 0L;
        Optional<Plan> topTierPlanOpt = planQueryRepository.findTopTierPlan();
        if (topTierPlanOpt.isPresent()) {
            Plan topTierPlan = topTierPlanOpt.get();
            topTierPlanName = topTierPlan.getName();
            topTierSubscribers = tenantQueryRepository.countActiveSubscribersByPlanId(topTierPlan.getId());
        }

        // 4. Calculate the current Monthly Recurring Revenue (MRR)
        Double currentRevenue = tenantQueryRepository.calculateMonthlyActivePlanRevenue();
        // 5. Calculate the Monthly Recurring Revenue (MRR) of the previous month
        Double lastMonthRevenue = tenantQueryRepository.calculateMonthlyActivePlanRevenueLastMonth();
        
        // 6. Calculate the revenue growth trend: % change in MRR compared to last month
        Double revenueTrend = 0.0;
        if (lastMonthRevenue > 0) {
            revenueTrend = ((currentRevenue - lastMonthRevenue) / lastMonthRevenue) * 100.0;
        } else if (currentRevenue > 0) {
            revenueTrend = 100.0;
        }

        // 7. Calculate the churn rate for the current quarter
        Double churnRate = tenantQueryRepository.calculateChurnRate();
        // 8. Renewal Rate = 100% - Churn Rate
        Double renewalRate = 100.0 - churnRate;
        // 9. Renewal rate trend compared to the target goal of 93.7%
        Double renewalRateTrend = renewalRate - 93.7;

        return DashboardStatsPlanResponse.builder()
                .activePlans(activePlans)
                .activePlansTrend(activePlansTrend)
                .topTierPlanName(topTierPlanName)
                .topTierSubscribers(topTierSubscribers)
                .monthlyActivePlanRevenue(currentRevenue)
                .monthlyActivePlanRevenueTrend(revenueTrend)
                .renewalRate(renewalRate)
                .renewalRateTrend(renewalRateTrend)
                .build();
    }
}
