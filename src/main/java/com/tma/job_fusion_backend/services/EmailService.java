package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;

public interface EmailService {
    void sendResetPasswordOtp(String toEmail, String otp);
    void sendTenantCreatedEmail(TenantCreatedEmailDto dto);
}
