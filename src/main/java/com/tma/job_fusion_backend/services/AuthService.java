package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.*;
import com.tma.job_fusion_backend.pojo.responses.AuthResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;

public interface AuthService {
    UserResponse signUp(SignUpRequest request);
    AuthResponse signIn(SignInRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void checkOTP(VerifyOtpRequest request);
}
