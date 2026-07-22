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
        Double totalRevenue = tenantQueryRepository.calculateTotalRevenue();
        Long activeTenants = tenantQueryRepository.countActiveTenants();
        Double averageUsage = tenantQueryRepository.calculateAverageUsage();
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
        Long activePlans = planQueryRepository.countActivePlans();
        Long activePlansTrend = planQueryRepository.countActivePlansCreatedThisMonth();

        String topTierPlanName = "";
        Long topTierSubscribers = 0L;
        Optional<Plan> topTierPlanOpt = planQueryRepository.findTopTierPlan();
        if (topTierPlanOpt.isPresent()) {
            Plan topTierPlan = topTierPlanOpt.get();
            topTierPlanName = topTierPlan.getName();
            topTierSubscribers = tenantQueryRepository.countActiveSubscribersByPlanId(topTierPlan.getId());
        }

        Double currentRevenue = tenantQueryRepository.calculateMonthlyActivePlanRevenue();
        Double lastMonthRevenue = tenantQueryRepository.calculateMonthlyActivePlanRevenueLastMonth();
        Double revenueTrend = 0.0;
        if (lastMonthRevenue > 0) {
            revenueTrend = ((currentRevenue - lastMonthRevenue) / lastMonthRevenue) * 100.0;
        } else if (currentRevenue > 0) {
            revenueTrend = 100.0;
        }

        Double churnRate = tenantQueryRepository.calculateChurnRate();
        Double renewalRate = 100.0 - churnRate;
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
