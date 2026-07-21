package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.EmploymentType;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.enums.LocationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type")
    private LocationType locationType;

    private String location;

    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;

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
