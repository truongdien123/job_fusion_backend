package com.tma.job_fusion_backend.pojo.responses;

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
    private String fullName;
    private String phone;
    private String headline;
    private String address;
    private LocalDateTime dateOfBirth;
    private String avatar;
    private String employeeCode;
    private String jobTitle;
    private UserStatus status;
    private UserType type;
    private String userRole;
    private UUID tenantId;
    private UUID planId;
    private LocalDateTime createdAt;
    private String officeLocation;
    private Boolean requirePasswordChange;
}
