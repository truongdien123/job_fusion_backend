package com.tma.job_fusion_backend.pojo.dtos;

import com.tma.job_fusion_backend.enums.TenantStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantFilter {
    private String search;
    private String companyName;
    private String domain;
    private String industry;
    private TenantStatus status;
}
