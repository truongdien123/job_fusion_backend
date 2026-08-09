package com.tma.job_fusion_backend.pojo.responses;

import com.tma.job_fusion_backend.enums.ApplicationStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplicationResponse {
    private UUID id;
    private UUID candidateId;
    private String candidateName;
    private String candidateEmail;
    private UUID jobId;
    private String jobTitle;
    private String department;
    private Double matchScore;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private Boolean reviewed;

}
