package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.requests.JobCriteriaRequest;
import com.tma.job_fusion_backend.pojo.responses.JobCriteriaResponse;
import com.tma.job_fusion_backend.services.JobCriteriaService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_JOB_CRITERIA)
@RequiredArgsConstructor
@Tag(name = "job-criteria")
public class JobCriteriaController {

    private final JobCriteriaService jobCriteriaService;

    @PostMapping
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> createJobCriteria(@Valid @RequestBody JobCriteriaRequest request) {
        JobCriteriaResponse response = jobCriteriaService.createJobCriteria(request);
        return ResponseUtil.success("Create job criteria successfully", response);
    }

    @GetMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR, RoleConstant.INTERVIEWER})
    public ResponseEntity<?> getJobCriteriaDetail(@PathVariable UUID id) {
        JobCriteriaResponse response = jobCriteriaService.getJobCriteriaDetail(id);
        return ResponseUtil.success("Get job criteria detail successfully", response);
    }

    @GetMapping("/job/{jobId}")
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR, RoleConstant.INTERVIEWER})
    public ResponseEntity<?> getJobCriteriaByJobId(@PathVariable UUID jobId) {
        List<JobCriteriaResponse> response = jobCriteriaService.getJobCriteriaByJobId(jobId);
        return ResponseUtil.success("Get job criteria by job successfully", response);
    }

    @PutMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> updateJobCriteria(@PathVariable UUID id, @Valid @RequestBody JobCriteriaRequest request) {
        JobCriteriaResponse response = jobCriteriaService.updateJobCriteria(id, request);
        return ResponseUtil.success("Update job criteria successfully", response);
    }

    @DeleteMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> deleteJobCriteria(@PathVariable UUID id) {
        jobCriteriaService.deleteJobCriteria(id);
        return ResponseUtil.success("Delete job criteria successfully", Boolean.TRUE);
    }

}
