package com.tma.job_fusion_backend.projections;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantUsageProjection {
    private Long activeUsers;
    private Integer maxUsers;
}
