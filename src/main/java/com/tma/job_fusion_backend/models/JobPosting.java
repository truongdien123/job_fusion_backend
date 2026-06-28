package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_postings", indexes = {
    @Index(name = "idx_job_postings_tenant_id", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String title;

    private String department;

    private String level;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "salary_min")
    private Double salaryMin;

    @Column(name = "salary_max")
    private Double salaryMax;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

}
