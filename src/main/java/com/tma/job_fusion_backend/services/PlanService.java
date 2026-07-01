package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.CreatePlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlanService {
    PlanResponse createPlanResponse(CreatePlanRequest request);
    Page<PlanResponse> getListPlan(Pageable pageable);
}
