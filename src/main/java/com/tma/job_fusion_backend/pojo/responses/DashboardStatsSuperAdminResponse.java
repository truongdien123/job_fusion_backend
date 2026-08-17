package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsSuperAdminResponse {
    private Long totalTenants;
    private Double totalTenantsTrend;
    private Long activeTenants;
    private Double activeTenantsTrend;
    private Double monthlyRecurringRevenue;
    private Double monthlyRecurringRevenueTrend;
    private Long tenantsExpiringWithin30Days;
}
