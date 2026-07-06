package com.tma.job_fusion_backend.pojo.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Monthly price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly price must be greater than or equal to 0")
    @JsonProperty("monthly_price")
    private Double monthlyPrice;

    @Min(value = 0, message = "Max staff account must be greater than or equal to 0")
    @JsonProperty("max_staff_account")
    private Integer maxStaffAccount;

    @NotNull(message = "Staff account unlimited flag is required")
    @JsonProperty("staff_account_unlimited")
    private Boolean staffAccountUnlimited;

    @Min(value = 0, message = "Max active job posting must be greater than or equal to 0")
    @JsonProperty("max_active_job_posting")
    private Integer maxActiveJobPosting;

    @NotNull(message = "Active job posting unlimited flag is required")
    @JsonProperty("active_job_posting_unlimited")
    private Boolean activeJobPostingUnlimited;

    private List<FeatureDto> features;
}
