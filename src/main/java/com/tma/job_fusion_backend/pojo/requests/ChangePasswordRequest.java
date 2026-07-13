package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Old password is required")
    @Size(max = 20, message = "Old password must be at most 20 characters long")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(max = 20, message = "New password must be at most 20 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,20}$",
        message = "New password must be between 8 and 20 characters long, containing at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String newPassword;
}
