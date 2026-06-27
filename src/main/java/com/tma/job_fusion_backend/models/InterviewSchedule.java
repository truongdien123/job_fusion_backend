package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.InterviewStatus;
import com.tma.job_fusion_backend.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private CandidateApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type")
    private InterviewType interviewType;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "meeting_link")
    private String meetingLink;

    private String location;

    @Column(columnDefinition = "TEXT", name = "ai_brief")
    private String aiBrief;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "ai_questions")
    private JsonNode aiQuestions;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

}
