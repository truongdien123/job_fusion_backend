package com.tma.job_fusion_backend.components;

import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.repositories.TenantRepository;
import com.tma.job_fusion_backend.repositories.query.UserRoleQueryRepository;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class TenantExpirationScheduler {

    private final TenantRepository tenantRepository;
    private final UserRoleQueryRepository userRoleQueryRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "${app.cron.check-tenant-expiration:0 0 1 * * *}") // Runs at 1:00 AM every day by default
    @Transactional
    public void checkTenantExpirations() {
        log.info("Starting cron job to check tenant expirations...");
        LocalDateTime now = DateTimeUtil.nowUtc();

        // 1. Process Warning Emails for tenants expiring in less than 7 days
        LocalDateTime warningThreshold = now.plusDays(7);
        List<Tenant> warningTenants = tenantRepository
                .findAllByStatusAndDeletedAtIsNullAndExpirationDateLessThanEqualAndExpirationWarningSentFalse(
                        TenantStatus.ACTIVE, warningThreshold);

        log.info("Found {} active tenants qualifying for expiration warnings.", warningTenants.size());

        for (Tenant tenant : warningTenants) {
            if (ObjectUtils.isEmpty(tenant.getExpirationDate())) {
                continue;
            }
            // Ensure expiration date is in the future relative to 'now'
            if (tenant.getExpirationDate().isAfter(now)) {
                long daysRemaining = ChronoUnit.DAYS.between(now, tenant.getExpirationDate());
                Optional<User> adminOpt = userRoleQueryRepository.findTenantAdminByTenantId(tenant.getId());
                if (adminOpt.isPresent()) {
                    String adminEmail = adminOpt.get().getEmail();
                    String formattedDate = tenant.getExpirationDate().format(DATE_FORMATTER);
                    try {
                        emailService.sendTenantExpirationWarningEmail(
                                adminEmail, tenant.getCompanyName(), formattedDate, daysRemaining);
                        tenant.setExpirationWarningSent(true);
                        tenantRepository.save(tenant);
                        log.info("Expiration warning email sent successfully to tenant admin {} of workspace {}.", 
                                adminEmail, tenant.getCompanyName());
                    } catch (Exception e) {
                        log.error("Failed to send warning email to {} of workspace {}: {}", 
                                adminEmail, tenant.getCompanyName(), e.getMessage());
                    }
                } else {
                    log.warn("Could not find Tenant Admin for tenant {}. Warning email not sent.", tenant.getCompanyName());
                }
            }
        }

        // 2. Process Deactivations for expired tenants
        List<Tenant> expiredTenants = tenantRepository
                .findAllByStatusAndDeletedAtIsNullAndExpirationDateBefore(TenantStatus.ACTIVE, now);

        log.info("Found {} active tenants that have expired.", expiredTenants.size());

        for (Tenant tenant : expiredTenants) {
            tenant.setStatus(TenantStatus.INACTIVE);
            tenantRepository.save(tenant);
            log.info("Tenant {} (ID: {}) status has been set to INACTIVE due to expiration.", 
                    tenant.getCompanyName(), tenant.getId());
        }

        log.info("Tenant expiration check cron job completed.");
    }
}
