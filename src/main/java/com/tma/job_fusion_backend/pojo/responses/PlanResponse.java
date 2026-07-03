package com.tma.job_fusion_backend.pojo.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponse {

    private UUID id;

    private String name;

    private String description;

    @JsonProperty("monthly_price")
    private Double monthlyPrice;

    @JsonProperty("max_staff_account")
    private Integer maxStaffAccount;

    @JsonProperty("staff_account_unlimited")
    private Boolean staffAccountUnlimited;

    @JsonProperty("max_active_job_posting")
    private Integer maxActiveJobPosting;

    @JsonProperty("active_job_posting_unlimited")
    private Boolean activeJobPostingUnlimited;

    private List<FeatureDto> features;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
