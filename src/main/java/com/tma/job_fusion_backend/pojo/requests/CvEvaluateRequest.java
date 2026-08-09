package com.tma.job_fusion_backend.pojo.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvEvaluateRequest {
    private String fileUrl;
    private List<JobCriterionInput> criteria;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobCriterionInput {
        private String criterionName;
        private String description;
        private Double weight;
    }
}
