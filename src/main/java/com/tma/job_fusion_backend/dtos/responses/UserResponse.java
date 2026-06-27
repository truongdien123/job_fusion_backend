package com.tma.job_fusion_backend.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    private String phone;
    private String headline;
    private String address;

    @JsonProperty("date_of_birth")
    private LocalDateTime dateOfBirth;

    private String avatar;

    @JsonProperty("employee_code")
    private String employeeCode;

    @JsonProperty("job_title")
    private String jobTitle;

    private UserStatus status;
    private UserType type;

    @JsonProperty("tenant_id")
    private UUID tenantId;
}
