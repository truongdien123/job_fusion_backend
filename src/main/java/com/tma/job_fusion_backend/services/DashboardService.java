package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.responses.DashboardStatsTenantResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsPlanResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsJobPostingResponse;

public interface DashboardService {
    DashboardStatsTenantResponse getDashboardStatsTenant();
    DashboardStatsPlanResponse getDashboardStatsPlan();
    DashboardStatsJobPostingResponse getDashboardStatsJobPosting();
}
