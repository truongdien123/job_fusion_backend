package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must contain exactly 6 digits")
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(max = 20, message = "New password must be at most 20 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,20}$",
        message = "Password must be between 8 and 20 characters long, containing at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String newPassword;
}
