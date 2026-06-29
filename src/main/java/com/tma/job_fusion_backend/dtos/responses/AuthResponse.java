package com.tma.job_fusion_backend.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;

    @JsonProperty("refresh_token")
    private String refreshToken;
    private UserResponse user;
}
