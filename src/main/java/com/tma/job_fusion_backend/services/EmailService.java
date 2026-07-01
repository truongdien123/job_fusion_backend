package com.tma.job_fusion_backend.services;

public interface EmailService {
    void sendResetPasswordOtp(String toEmail, String otp);
}
