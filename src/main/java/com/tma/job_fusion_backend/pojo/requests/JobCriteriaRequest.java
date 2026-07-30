package com.tma.job_fusion_backend.pojo.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobCriteriaRequest {

    private UUID id;

    @NotNull(message = "Job ID is required")
    private UUID jobId;

    @NotBlank(message = "Criterion name is required")
    @Size(max = 255, message = "Criterion name length cannot exceed 255 characters")
    private String criterionName;

    @Size(max = 255, message = "Description length cannot exceed 255 characters")
    private String description;

    @Size(max = 255, message = "Category length cannot exceed 255 characters")
    private String category;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
    private Double weight;

}
