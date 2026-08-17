package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsTenantResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsPlanResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsJobPostingResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsSuperAdminResponse;
import com.tma.job_fusion_backend.repositories.query.TenantQueryRepository;
import com.tma.job_fusion_backend.repositories.query.PlanQueryRepository;
import com.tma.job_fusion_backend.repositories.query.JobPostingQueryRepository;
import com.tma.job_fusion_backend.services.DashboardService;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.commons.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TenantQueryRepository tenantQueryRepository;
    private final PlanQueryRepository planQueryRepository;
    private final JobPostingQueryRepository jobPostingQueryRepository;
    private final ValidationUtil validationUtil;

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
        Double currentRevenue = getMonthlyActivePlanRevenue();
        // 5. Calculate the Monthly Recurring Revenue Trend
        Double revenueTrend = getMonthlyRecurringRevenueTrend(currentRevenue);

        // 7. Calculate the churn rate for the current quarter
        Double churnRate = tenantQueryRepository.calculateChurnRate();
        // 8. Renewal Rate = 100% - Churn Rate
        double renewalRate = 100.0 - churnRate;
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

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsJobPostingResponse getDashboardStatsJobPosting() {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        return jobPostingQueryRepository.getJobPostingStats(tenantId, 7);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsSuperAdminResponse getDashboardStatsSuperAdmin() {
        // 1. Calculate Total Tenants (current and last month) and compute trend
        Long currentTotalTenants = tenantQueryRepository.countTotalTenants();
        Long lastMonthTotalTenants = tenantQueryRepository.countTotalTenantsLastMonth();
        double totalTenantsTrend = 0.0;
        if (lastMonthTotalTenants > 0) {
            totalTenantsTrend = ((double) (currentTotalTenants - lastMonthTotalTenants) / lastMonthTotalTenants) * 100.0;
        } else if (currentTotalTenants > 0) {
            totalTenantsTrend = 100.0;
        }

        // 2. Calculate Active Tenants (current and last month) and compute trend
        Long currentActiveTenants = tenantQueryRepository.countActiveTenants();
        Long lastMonthActiveTenants = tenantQueryRepository.countActiveTenantsLastMonth();
        double activeTenantsTrend = 0.0;
        if (lastMonthActiveTenants > 0) {
            activeTenantsTrend = ((double) (currentActiveTenants - lastMonthActiveTenants) / lastMonthActiveTenants) * 100.0;
        } else if (currentActiveTenants > 0) {
            activeTenantsTrend = 100.0;
        }

        // 3. Calculate Monthly Recurring Revenue (current and last month) and compute trend
        Double currentMRR = getMonthlyActivePlanRevenue();
        Double mrrTrend = getMonthlyRecurringRevenueTrend(currentMRR);

        // 4. Calculate Tenants expiring within 30 days
        Long tenantsExpiringWithin30Days = tenantQueryRepository.countTenantsExpiringWithin30Days();

        return DashboardStatsSuperAdminResponse.builder()
                .totalTenants(currentTotalTenants)
                .totalTenantsTrend(totalTenantsTrend)
                .activeTenants(currentActiveTenants)
                .activeTenantsTrend(activeTenantsTrend)
                .monthlyRecurringRevenue(currentMRR)
                .monthlyRecurringRevenueTrend(mrrTrend)
                .tenantsExpiringWithin30Days(Objects.requireNonNullElse(tenantsExpiringWithin30Days, 0L))
                .build();
    }

    private double getMonthlyActivePlanRevenue() {
        return tenantQueryRepository.calculateMonthlyActivePlanRevenue();
    }

    private double getMonthlyRecurringRevenueTrend(Double currentMRR) {
        double lastMonthMRR = tenantQueryRepository.calculateMonthlyActivePlanRevenueLastMonth();
        double mrrTrend = 0.0;
        if (lastMonthMRR > 0) {
            mrrTrend = ((currentMRR - lastMonthMRR) / lastMonthMRR) * 100.0;
        } else if (currentMRR > 0) {
            mrrTrend = 100.0;
        }
        return mrrTrend;
    }
}

