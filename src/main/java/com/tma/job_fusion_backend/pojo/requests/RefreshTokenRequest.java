package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Size(max = 2000, message = "Refresh token length cannot exceed 2000 characters")
    private String refreshToken;
}
