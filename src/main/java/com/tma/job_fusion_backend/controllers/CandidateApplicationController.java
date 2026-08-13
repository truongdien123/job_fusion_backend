package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.dtos.CandidateApplicationFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.UpdateApplicationStatusRequest;
import com.tma.job_fusion_backend.pojo.responses.CandidateApplicationResponse;
import jakarta.validation.Valid;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.services.CandidateApplicationService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_CANDIDATE_APPLICATION)
@RequiredArgsConstructor
@Tag(name = "candidate-application")
public class CandidateApplicationController {

    private final CandidateApplicationService candidateApplicationService;

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR, RoleConstant.INTERVIEWER, RoleConstant.CANDIDATE})
    public ResponseEntity<?> getListCandidateApplications(@RequestBody PagingRequest<CandidateApplicationFilter> request) {
        PageResponse<CandidateApplicationResponse> response = candidateApplicationService.getApplications(request);
        return ResponseUtil.success("Get candidate applications successfully", response);
    }

    @PatchMapping(EndpointConstant.ENDPOINT_ID + "/review")
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> markAsReviewed(@PathVariable UUID id) {
        candidateApplicationService.markAsReviewed(id);
        return ResponseUtil.success("Candidate application marked as reviewed successfully", Boolean.TRUE);
    }

    @PatchMapping(EndpointConstant.ENDPOINT_APPLICATION_STATUS)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        candidateApplicationService.updateStatus(id, request);
        return ResponseUtil.success("Candidate application status updated successfully", Boolean.TRUE);
    }

    @GetMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR, RoleConstant.INTERVIEWER, RoleConstant.CANDIDATE})
    public ResponseEntity<?> getCandidateApplicationDetail(@PathVariable UUID id) {
        CandidateApplicationResponse response = candidateApplicationService.getApplicationDetail(id);
        return ResponseUtil.success("Get candidate application detail successfully", response);
    }
}
