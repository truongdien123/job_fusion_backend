package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCriteriaResponse {

    private UUID id;

    private UUID jobId;

    private String criterionName;

    private String description;

    private String category;

    private Double weight;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
