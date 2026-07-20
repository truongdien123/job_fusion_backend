package com.tma.job_fusion_backend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tma.job_fusion_backend.enums.EmailStatus;
import com.tma.job_fusion_backend.models.EmailLog;
import com.tma.job_fusion_backend.repositories.EmailLogRepository;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.pojo.dtos.TenantCreatedEmailDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.tma.job_fusion_backend.utils.DateTimeUtil;

@Service
@Log4j2
public class EmailServiceImpl implements EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    private final EmailLogRepository emailLogRepository;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public EmailServiceImpl(EmailLogRepository emailLogRepository, TemplateEngine templateEngine) {
        this.emailLogRepository = emailLogRepository;
        this.templateEngine = templateEngine;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
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
        String subject;
        String roleStr = dto.getRole() != null ? dto.getRole().toUpperCase() : "";

        boolean isTenantAdmin = roleStr.contains("TENANT_ADMIN");
        boolean isHr = roleStr.contains("HR");
        boolean isInterviewer = roleStr.contains("INTERVIEWER");

        if (isTenantAdmin) {
            subject = "Your new Tenant workspace is ready";
        } else {
            subject = "Invitation to join " + dto.getTenantName() + " on JobFusion";
        }

        Context context = new Context();
        context.setVariable("adminName", dto.getAdminName());
        context.setVariable("tenantName", dto.getTenantName());
        context.setVariable("adminEmail", dto.getToEmail());
        context.setVariable("adminPassword", dto.getAdminPassword());
        context.setVariable("role", dto.getRole());
        context.setVariable("activationUrl", dto.getActivationUrl());
        context.setVariable("isTenantAdmin", isTenantAdmin);
        context.setVariable("isHr", isHr);
        context.setVariable("isInterviewer", isInterviewer);

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

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            
            ArrayNode personalizations = objectMapper.createArrayNode();
            ObjectNode personalization = objectMapper.createObjectNode();
            ArrayNode toArray = objectMapper.createArrayNode();
            toArray.add(objectMapper.createObjectNode().put("email", toEmail));
            personalization.set("to", toArray);
            personalizations.add(personalization);
            payload.set("personalizations", personalizations);

            payload.set("from", objectMapper.createObjectNode()
                    .put("email", fromEmail)
                    .put("name", "Job Fusion"));
            
            payload.put("subject", subject);

            ArrayNode contentArray = objectMapper.createArrayNode();
            contentArray.add(objectMapper.createObjectNode()
                    .put("type", "text/html")
                    .put("value", htmlBody));
            payload.set("content", contentArray);
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                emailLog.setStatus(EmailStatus.SENT);
                log.info("Sent {} HTML email to {} successfully via SendGrid API.", emailType, toEmail);
            } else {
                emailLog.setStatus(EmailStatus.FAILED);
                log.error("Failed to send email via SendGrid API. Status: {}, Response: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            emailLog.setStatus(EmailStatus.FAILED);
            log.error("Exception occurred while sending email to {}: {}", toEmail, e.getMessage());
        }

        emailLogRepository.save(emailLog);
    }
}
