package com.tma.job_fusion_backend.pojo.dtos;

import com.tma.job_fusion_backend.enums.ApplicationStatus;
import com.tma.job_fusion_backend.enums.MatchScoreLevel;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplicationFilter {
    private String search;
    private UUID jobId;
    private MatchScoreLevel matchScoreLevel;
    private LocalDateTime appliedDateFrom;
    private LocalDateTime appliedDateTo;
    private ApplicationStatus status;
}
