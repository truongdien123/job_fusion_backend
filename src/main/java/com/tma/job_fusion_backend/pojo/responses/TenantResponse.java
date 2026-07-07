package com.tma.job_fusion_backend.pojo.responses;

import com.tma.job_fusion_backend.enums.TenantStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {
    private UUID id;

    private String companyName;
    private String domain;
    private String industry;

    private Integer companySize;
    private String region;
    private TenantStatus status;

    private UUID planId;

    private UUID adminUserId;
}
