package com.tma.job_fusion_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobCriteria extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private JobPosting job;

    @Column(name = "criterion_name")
    private String criterionName;

    private String description;

    private String category;

    private Double weight;

}
