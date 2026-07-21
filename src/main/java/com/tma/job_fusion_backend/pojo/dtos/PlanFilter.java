package com.tma.job_fusion_backend.pojo.dtos;

import com.tma.job_fusion_backend.enums.PlanStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanFilter {
    private String search;
    private String name;
    private String description;
    private PlanStatus status;
}
