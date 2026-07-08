package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.enums.TenantStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantRequest {
    private String companyName;
    private String domain;
    private String industry;
    private String region;
    private TenantStatus status;
    private UUID planId;
}
