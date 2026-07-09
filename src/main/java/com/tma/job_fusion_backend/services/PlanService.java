package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.CreatePlanRequest;
import com.tma.job_fusion_backend.pojo.requests.UpdatePlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PlanService {
    PlanResponse createPlan(CreatePlanRequest request);
    Page<PlanResponse> getListPlan(Pageable pageable);
    PlanResponse getPlanDetail(UUID id);
    PlanResponse updatePlan(UUID id, UpdatePlanRequest request);
}
