package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.dtos.requests.SignUpRequest;
import com.tma.job_fusion_backend.dtos.requests.SignInRequest;
import com.tma.job_fusion_backend.dtos.responses.AuthResponse;
import com.tma.job_fusion_backend.dtos.responses.UserResponse;

public interface AuthService {
    UserResponse signUp(SignUpRequest request);
    AuthResponse signIn(SignInRequest request);
}
