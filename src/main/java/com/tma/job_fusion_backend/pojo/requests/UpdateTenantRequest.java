package com.tma.job_fusion_backend.pojo.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tma.job_fusion_backend.enums.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTenantRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String domain;

    private String industry;

    private String region;

    private TenantStatus status;

    private UUID planId;
}
