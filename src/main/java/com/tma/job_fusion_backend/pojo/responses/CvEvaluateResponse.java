package com.tma.job_fusion_backend.pojo.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvEvaluateResponse {
    private JsonNode parsedData;
    private Double candidateSelfScore;
    private JsonNode cvImprovementSuggestions;
}
