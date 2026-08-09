package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;
import tools.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResumeResponse {
    private UUID id;
    private UUID userId;
    private String fileUrl;
    private JsonNode parsedData;
    private Double candidateSelfScore;
    private JsonNode cvImprovementSuggestions;
    private Double matchingScore;
    private JsonNode reasoning;
    private JsonNode skillGaps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
