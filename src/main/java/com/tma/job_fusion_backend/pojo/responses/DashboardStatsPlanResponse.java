package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsPlanResponse {
    private Long activePlans;
    private Long activePlansTrend;
    private String topTierPlanName;
    private Long topTierSubscribers;
    private Double monthlyActivePlanRevenue;
    private Double monthlyActivePlanRevenueTrend;
    private Double renewalRate;
    private Double renewalRateTrend;
}
