package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.commons.validation.OnCreate;
import com.tma.job_fusion_backend.commons.validation.OnUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import com.tma.job_fusion_backend.enums.UserStatus;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRequest {

    @NotBlank(message = "Email is required", groups = {OnCreate.class})
    @Email(message = "Invalid email format", groups = {OnCreate.class})
    private String email;

    @NotBlank(message = "Full name is required", groups = {OnCreate.class, OnUpdate.class})
    private String fullName;

    @NotEmpty(message = "Role is required", groups = {OnCreate.class, OnUpdate.class})
    private List<@Pattern(regexp = "^(HR|Interviewer)$", message = "Role must be either HR or Interviewer", groups = {OnCreate.class, OnUpdate.class}) String> role;

    private UserStatus status;
}
