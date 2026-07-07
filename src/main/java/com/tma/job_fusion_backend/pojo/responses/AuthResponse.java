package com.tma.job_fusion_backend.pojo.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;

    private String refreshToken;
    private UserResponse user;
}
