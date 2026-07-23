package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCriteriaRequest {

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @NotBlank(message = "Criterion name is required")
    private String criterionName;

    private String description;

    private String category;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
    private Double weight;

}
