package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import com.tma.job_fusion_backend.enums.UserStatus;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email length cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name length cannot exceed 255 characters")
    private String fullName;

    @NotEmpty(message = "Role is required")
    private List<@Pattern(regexp = "^(HR|Interviewer)$", message = "Role must be either HR or Interviewer") String> role;

    private UserStatus status;
}
