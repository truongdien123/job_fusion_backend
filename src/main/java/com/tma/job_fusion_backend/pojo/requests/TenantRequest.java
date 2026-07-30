package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.enums.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @Size(max = 255, message = "Domain length cannot exceed 255 characters")
    private String domain;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    @NotBlank(message = "Region is required")
    @Size(max = 255, message = "Region length cannot exceed 255 characters")
    private String region;

    // For create only
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid admin email format")
    @Size(max = 255, message = "Admin email length cannot exceed 255 characters")
    private String adminEmail;

    @NotBlank(message = "Admin full name is required")
    @Size(max = 255, message = "Admin full name length cannot exceed 255 characters")
    private String adminFullName;

    @NotBlank(message = "Industry is required")
    @Size(max = 255, message = "Industry length cannot exceed 255 characters")
    private String industry;

    private TenantStatus status;
}
