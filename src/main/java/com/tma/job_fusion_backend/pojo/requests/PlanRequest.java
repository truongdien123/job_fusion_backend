package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.commons.validation.OnCreate;
import com.tma.job_fusion_backend.commons.validation.OnUpdate;
import com.tma.job_fusion_backend.enums.PlanStatus;
import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanRequest {

    @NotBlank(message = "Plan name is required", groups = {OnCreate.class, OnUpdate.class})
    @Size(max = 255, message = "Plan name must not exceed 255 characters", groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters", groups = {OnCreate.class, OnUpdate.class})
    private String description;

    @NotNull(message = "Monthly price is required", groups = {OnCreate.class, OnUpdate.class})
    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly price must be greater than or equal to 0", groups = {OnCreate.class, OnUpdate.class})
    private Double monthlyPrice;

    @Min(value = 0, message = "Max staff account must be greater than or equal to 0", groups = {OnCreate.class, OnUpdate.class})
    private Integer maxStaffAccount;

    @NotNull(message = "Staff account unlimited flag is required", groups = {OnCreate.class, OnUpdate.class})
    private Boolean staffAccountUnlimited;

    @Min(value = 0, message = "Max active job posting must be greater than or equal to 0", groups = {OnCreate.class, OnUpdate.class})
    private Integer maxActiveJobPosting;

    @NotNull(message = "Active job posting unlimited flag is required", groups = {OnCreate.class, OnUpdate.class})
    private Boolean activeJobPostingUnlimited;

    private PlanStatus status;

    private List<FeatureDto> features;
}
