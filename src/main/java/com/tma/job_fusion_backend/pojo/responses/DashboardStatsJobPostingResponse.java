package com.tma.job_fusion_backend.pojo.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsJobPostingResponse {
    private Long totalActivePostings;
    private Long totalApplicants;
    private Long postingsExpiringSoon;
}
