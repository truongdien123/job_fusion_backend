package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsTenantResponse {
    private Double totalRevenue;
    private Long activeTenants;
    private Double averageUsage;
    private Double churnRate;
}
