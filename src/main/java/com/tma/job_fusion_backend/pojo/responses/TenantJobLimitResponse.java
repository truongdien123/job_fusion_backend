package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantJobLimitResponse {
    private Long currentActiveJobs;
    private Integer maxActiveJobs;
}
