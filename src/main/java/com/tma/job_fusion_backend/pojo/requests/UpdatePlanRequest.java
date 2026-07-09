package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlanRequest implements PlanRequest {
    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly price must be greater than or equal to 0")
    private Double monthlyPrice;

    @Min(value = 0, message = "Max staff account must be greater than or equal to 0")
    private Integer maxStaffAccount;

    private Boolean staffAccountUnlimited;

    @Min(value = 0, message = "Max active job posting must be greater than or equal to 0")
    private Integer maxActiveJobPosting;

    private Boolean activeJobPostingUnlimited;

    private List<FeatureDto> features;
}
