package com.tma.job_fusion_backend.pojo.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantCreatedEmailDto {
    private String toEmail;
    private String adminName;
    private String tenantName;
    private String dashboardImageUrl;
    private String adminPassword;
    private String role;
    private String activationUrl;
}
