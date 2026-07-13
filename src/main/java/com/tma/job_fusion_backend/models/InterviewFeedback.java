package com.tma.job_fusion_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "interview_feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFeedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_schedule_id")
    private InterviewSchedule interviewSchedule;

    private Double score;

    private String strengths;

    private String weaknesses;

    private String recommendation;

    @Column(columnDefinition = "TEXT")
    private String comments;

}
