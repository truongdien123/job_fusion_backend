package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.BillingCycle;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends BaseEntity {

    @Column(name = "company_name")
    private String companyName;

    private String domain;

    private String industry;

    @Column(name = "company_size")
    private Integer companySize;

    private String region;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "max_staff_account")
    private Integer maxStaffAccount;

    @Column(name = "max_active_job_posting")
    private Integer maxActiveJobPosting;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "price")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle")
    private BillingCycle billingCycle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode feature;

}
