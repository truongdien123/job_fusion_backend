package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivationRequest {

    @NotBlank(message = "Token is required")
    @Size(max = 255, message = "Token length cannot exceed 255 characters")
    private String token;
}
