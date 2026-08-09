package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.dtos.JobPostingFilter;
import com.tma.job_fusion_backend.pojo.requests.JobPostingRequest;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.JobPostingResponse;
import com.tma.job_fusion_backend.pojo.requests.JdGenerateRequest;
import com.tma.job_fusion_backend.pojo.responses.JdGenerateResponse;
import com.tma.job_fusion_backend.pojo.responses.TenantJobLimitResponse;
import com.tma.job_fusion_backend.services.JobPostingService;
import com.tma.job_fusion_backend.services.JdAiService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_JOB_POSTING)
@RequiredArgsConstructor
@Tag(name = "job-posting")
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final JdAiService jdAiService;

    @PostMapping
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> createJobPosting(@Valid @RequestBody JobPostingRequest request) {
        JobPostingResponse response = jobPostingService.createJobPosting(request);
        return ResponseUtil.success("Create job posting successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_GENERATE_JD)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> generateJd(@Valid @RequestBody JdGenerateRequest request) {
        JdGenerateResponse response = jdAiService.generateJd(request);
        return ResponseUtil.success("Job description generated successfully", response);
    }

    @GetMapping(EndpointConstant.ENDPOINT_LIMIT)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> getTenantJobLimit() {
        TenantJobLimitResponse response = jobPostingService.getTenantJobLimit();
        return ResponseUtil.success("Get tenant job limit successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR, RoleConstant.INTERVIEWER, RoleConstant.CANDIDATE})
    public ResponseEntity<?> getListJobPosting(@RequestBody PagingRequest<JobPostingFilter> request) {
        PageResponse<JobPostingResponse> response = jobPostingService.getListJobPosting(request);
        return ResponseUtil.success("Get job posting list successfully", response);
    }

    @GetMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR, RoleConstant.INTERVIEWER, RoleConstant.CANDIDATE})
    public ResponseEntity<?> getJobPostingDetail(@PathVariable UUID id) {
        JobPostingResponse response = jobPostingService.getJobPostingDetail(id);
        return ResponseUtil.success("Get job posting detail successfully", response);
    }

    @PutMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> updateJobPosting(@PathVariable UUID id, @Valid @RequestBody JobPostingRequest request) {
        JobPostingResponse response = jobPostingService.updateJobPosting(id, request);
        return ResponseUtil.success("Update job posting successfully", response);
    }

    @DeleteMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> deleteJobPosting(@PathVariable UUID id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseUtil.success("Delete job posting successfully", Boolean.TRUE);
    }

    @GetMapping(EndpointConstant.ENDPOINT_CHECK_TITLE)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> checkTitleUniqueness(@RequestParam String title, @RequestParam(required = false) UUID excludeId) {
        jobPostingService.checkTitleUniqueness(title, excludeId);
        return ResponseUtil.success("Job title is unique", Boolean.TRUE);
    }
}
