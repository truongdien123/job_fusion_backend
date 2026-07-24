package com.tma.job_fusion_backend.pojo.responses;

import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.BillingCycle;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {
    private UUID id;
    private String companyName;
    private String domain;
    private String industry;
    private Integer companySize;
    private String region;
    private TenantStatus status;
    private UUID planId;
    private String planName;
    private Long activeUsers;
    private Integer maxUsers;
    private UUID adminUserId;
    private Long activeJob;
    private Integer maxActiveJobPosting;
    private Double price;
    private BillingCycle billingCycle;
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;

    public LocalDateTime getStartDate() {
        if (expirationDate != null) {
            if (billingCycle == BillingCycle.YEARLY) {
                return expirationDate.minusDays(365);
            }
            return expirationDate.minusDays(30);
        }
        return null;
    }
}
