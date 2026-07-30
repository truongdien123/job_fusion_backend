package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.annotations.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email length cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name length cannot exceed 255 characters")
    private String fullName;

    @Size(max = 255, message = "Phone length cannot exceed 255 characters")
    private String phone;
}

