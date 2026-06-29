package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.TenantStatus;
import jakarta.persistence.*;
import lombok.*;

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
    private String companySize;

    private String region;

    @Column(unique = true, name = "company_code")
    private String companyCode;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

}
