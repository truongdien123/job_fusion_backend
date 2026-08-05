package com.tma.job_fusion_backend.pojo.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriteriaAiGenerateRequest {
    private String jobTitle;
    private String description;
    private String requirements;
}
