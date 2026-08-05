package com.tma.job_fusion_backend.pojo.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaAiGenerateResponse {

    private List<Criterion> criteria;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Criterion {
        private String criterionName;

        private String description;

        private String category;

        private Double weight;
    }

}
