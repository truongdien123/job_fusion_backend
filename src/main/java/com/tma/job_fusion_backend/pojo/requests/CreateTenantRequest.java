package com.tma.job_fusion_backend.pojo.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CreateTenantRequest {

    @NotBlank(message = "Company name is required")
    @JsonProperty("company_name")
    private String companyName;

    private String domain;

    @NotNull(message = "Plan ID is required")
    @JsonProperty("plan_id")
    private UUID planId;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid admin email format")
    @JsonProperty("admin_email")
    private String adminEmail;

    @NotBlank(message = "Admin full name is required")
    @JsonProperty("admin_full_name")
    private String adminFullName;

}
