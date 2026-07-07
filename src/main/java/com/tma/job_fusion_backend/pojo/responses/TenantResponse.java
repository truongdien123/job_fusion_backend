package com.tma.job_fusion_backend.pojo.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("company_name")
    private String companyName;
    private String domain;
    private String industry;

    @JsonProperty("company_size")
    private Integer companySize;
    private String region;
    private TenantStatus status;

    @JsonProperty("plan_id")
    private UUID planId;

    @JsonProperty("admin_user_id")
    private UUID adminUserId;
}
