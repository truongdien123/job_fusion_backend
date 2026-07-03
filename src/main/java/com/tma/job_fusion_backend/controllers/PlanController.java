package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.requests.CreatePlanRequest;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import com.tma.job_fusion_backend.services.PlanService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_PLAN)
@RequiredArgsConstructor
@Tag(name = "plan")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        PlanResponse response = planService.createPlan(request);
        return ResponseUtil.success("Create plan successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> getListPlan(@RequestBody PagingRequest<?> request) {
        Pageable pageable = request.toPageable();
        Page<PlanResponse> listPlan = planService.getListPlan(pageable);
        return ResponseUtil.success("Get plans successfully", PageResponse.of(listPlan));
    }
}
