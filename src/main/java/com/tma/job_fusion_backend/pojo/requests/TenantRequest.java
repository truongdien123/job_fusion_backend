package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.commons.validation.OnCreate;
import com.tma.job_fusion_backend.commons.validation.OnUpdate;
import com.tma.job_fusion_backend.enums.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRequest {

    @NotBlank(message = "Company name is required", groups = {OnCreate.class})
    private String companyName;

    private String domain;

    @NotNull(message = "Plan ID is required", groups = {OnCreate.class})
    private UUID planId;

    @NotBlank(message = "Region is required", groups = {OnCreate.class})
    private String region;

    // For create only
    @NotBlank(message = "Admin email is required", groups = {OnCreate.class})
    @Email(message = "Invalid admin email format", groups = {OnCreate.class})
    private String adminEmail;

    @NotBlank(message = "Admin full name is required", groups = {OnCreate.class})
    private String adminFullName;

    @NotBlank(message = "Industry is required", groups = {OnCreate.class})
    private String industry;

    private TenantStatus status;
}
