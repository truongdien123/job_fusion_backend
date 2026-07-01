package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.enums.EmailStatus;
import com.tma.job_fusion_backend.models.EmailLog;
import com.tma.job_fusion_backend.repositories.EmailLogRepository;
import com.tma.job_fusion_backend.services.EmailService;
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
import java.time.ZoneOffset;

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
        String htmlBody = templateEngine.process("otp-email", context);

        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(toEmail);
        emailLog.setSubject(subject);
        emailLog.setBody(htmlBody);
        emailLog.setSentAt(LocalDateTime.now(ZoneOffset.UTC));

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
                log.info("Sent OTP HTML email to {} successfully.", toEmail);
            } catch (Exception e) {
                emailLog.setStatus(EmailStatus.FAILED);
                log.error("Failed to send OTP HTML email to {}: {}", toEmail, e.getMessage());
            }
        } else {
            log.warn("[NO SMTP CONFIG] Simulated OTP HTML email to {} (HTML logged in database). Code: {}", toEmail, otp);
            emailLog.setStatus(EmailStatus.FAILED);
        }

        emailLogRepository.save(emailLog);
    }
}
