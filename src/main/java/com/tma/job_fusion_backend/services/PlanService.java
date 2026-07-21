package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.PlanFilter;
import com.tma.job_fusion_backend.pojo.requests.PlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PlanService {
    PlanResponse createPlan(PlanRequest request);
    Page<PlanResponse> getListPlan(PlanFilter filter, Pageable pageable);
    PlanResponse getPlanDetail(UUID id);
    PlanResponse updatePlan(UUID id, PlanRequest request);
}
