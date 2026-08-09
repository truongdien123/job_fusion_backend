package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_applications", indexes = {
    @Index(name = "idx_candidate_applications_job_posting_id", columnList = "job_posting_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    private JobPosting job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private CandidateResume resume;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "reviewed", nullable = false)
    private Boolean reviewed = false;

}
