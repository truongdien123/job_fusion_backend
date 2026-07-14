package com.tma.job_fusion_backend.pojo.responses;

import com.tma.job_fusion_backend.enums.PlanStatus;
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

    private Double monthlyPrice;

    private Integer maxStaffAccount;

    private Boolean staffAccountUnlimited;

    private Integer maxActiveJobPosting;

    private Boolean activeJobPostingUnlimited;

    private PlanStatus status;

    private List<FeatureDto> features;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
