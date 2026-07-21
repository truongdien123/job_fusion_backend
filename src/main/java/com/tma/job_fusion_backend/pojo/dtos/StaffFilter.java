package com.tma.job_fusion_backend.pojo.dtos;

import com.tma.job_fusion_backend.enums.UserStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffFilter {
    private String search;
    private String fullName;
    private String email;
    private String phone;
    private String employeeCode;
    private String jobTitle;
    private UserStatus status;
}
