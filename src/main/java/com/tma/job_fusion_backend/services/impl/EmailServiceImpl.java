package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.enums.EmailStatus;
import com.tma.job_fusion_backend.models.EmailLog;
import com.tma.job_fusion_backend.repositories.EmailLogRepository;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import com.tma.job_fusion_backend.utils.DateTimeUtil;

@Service
@Log4j2
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender, EmailLogRepository emailLogRepository, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
        this.templateEngine = templateEngine;
    }


    @Override
    @Async("mailExecutor")
    public void sendResetPasswordOtp(String toEmail, String otp) {
        String subject = "Reset Password OTP";

        Context context = new Context();
        context.setVariable("otp", otp);
        sendHtmlEmail(toEmail,
                subject,
                "otp-email",
                context,
                "OTP",
                "Code: " + otp);
    }

    @Override
    @Async("mailExecutor")
    public void sendTenantCreatedEmail(TenantCreatedEmailDto dto) {
        String subject = "Your new Tenant workspace is ready";

        Context context = new Context();
        context.setVariable("adminName", dto.getAdminName());
        context.setVariable("tenantName", dto.getTenantName());
        context.setVariable("loginUrl", dto.getLoginUrl());
        context.setVariable("dashboardImageUrl", dto.getDashboardImageUrl());
        context.setVariable("adminEmail", dto.getToEmail());
        context.setVariable("adminPassword", dto.getAdminPassword());
        sendHtmlEmail(dto.getToEmail(),
                subject,
                "tenant-created-email",
                context,
                "Tenant Created",
                "Tenant: " + dto.getTenantName());

    }

    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context, String emailType, String simulatedSuffix) {
        String htmlBody = templateEngine.process(templateName, context);

        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(toEmail);
        emailLog.setSubject(subject);
        emailLog.setBody(htmlBody);
        emailLog.setSentAt(DateTimeUtil.nowUtc());

        if (mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom("Job Fusion <" + from + ">");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);

                mailSender.send(mimeMessage);

                emailLog.setStatus(EmailStatus.SENT);
                log.info("Sent Tenant Created HTML email to {} successfully.", toEmail);
            } catch (Exception e) {
                emailLog.setStatus(EmailStatus.FAILED);
                log.error("Failed to send Tenant Created HTML email to {}: {}", toEmail, e.getMessage());
            }
        } else {
            log.warn("[NO SMTP CONFIG] Simulated {} HTML email to {} (HTML logged in database). {}",
                    emailType, toEmail, simulatedSuffix);
            emailLog.setStatus(EmailStatus.FAILED);
        }

        emailLogRepository.save(emailLog);
    }
}
