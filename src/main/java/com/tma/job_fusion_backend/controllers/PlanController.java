package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.UserTypeConstant;
import com.tma.job_fusion_backend.pojo.requests.CreatePlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import com.tma.job_fusion_backend.services.PlanService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_PLAN)
@RequiredArgsConstructor
@Tag(name = "plan")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @PreAuthorize(UserTypeConstant.PLATFORM)
    public ResponseEntity<?> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        PlanResponse response = planService.createPlanResponse(request);
        return ResponseUtil.success("Create plan successfully", response);
    }

    @GetMapping
    @PreAuthorize(UserTypeConstant.PLATFORM)
    public ResponseEntity<?> getListPlan(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PlanResponse> listPlan = planService.getListPlan(pageable);
        return ResponseUtil.success("Get plans successfully", PageResponse.of(listPlan));
    }
}
