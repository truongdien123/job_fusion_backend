package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivationRequest {

    @NotBlank(message = "Token is required")
    private String token;
}
