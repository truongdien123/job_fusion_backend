package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.annotations.ValidPassword;
import jakarta.validation.constraints.NotBlank;
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
    @ValidPassword
    private String newPassword;
}

