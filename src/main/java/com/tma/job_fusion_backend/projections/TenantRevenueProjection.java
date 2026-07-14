package com.tma.job_fusion_backend.projections;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantRevenueProjection {
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private Double monthlyPrice;
}
