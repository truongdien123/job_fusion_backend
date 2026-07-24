package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.enums.PlanStatus;
import com.tma.job_fusion_backend.enums.BillingCycle;
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

    @NotBlank(message = "Plan name is required")
    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private Double price;

    @Min(value = 1, message = "Max staff account must be greater than or equal to 1")
    private Integer maxStaffAccount;

    @NotNull(message = "Staff account unlimited flag is required")
    private Boolean staffAccountUnlimited;

    @Min(value = 1, message = "Max active job posting must be greater than or equal to 1")
    private Integer maxActiveJobPosting;

    @NotNull(message = "Active job posting unlimited flag is required")
    private Boolean activeJobPostingUnlimited;

    @NotNull(message = "Status is required")
    private PlanStatus status;

    @NotEmpty(message = "Features list is required")
    private List<FeatureDto> features;
}
