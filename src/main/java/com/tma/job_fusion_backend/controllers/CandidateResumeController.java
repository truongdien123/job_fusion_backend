package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.responses.CandidateResumeResponse;
import com.tma.job_fusion_backend.services.CandidateResumeService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_CANDIDATE_RESUME)
@RequiredArgsConstructor
@Tag(name = "candidate-resume")
public class CandidateResumeController {

    private final CandidateResumeService candidateResumeService;

    @PostMapping(value = EndpointConstant.ENDPOINT_JOB_ID, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireRoles(RoleConstant.CANDIDATE)
    public ResponseEntity<?> uploadResume(
            @PathVariable("jobId") UUID jobId,
            @RequestPart("file") MultipartFile file) {
        CandidateResumeResponse response = candidateResumeService.uploadResume(jobId, file);
        return ResponseUtil.success("Resume uploaded successfully", response);
    }

    @GetMapping(EndpointConstant.ENDPOINT_JOB_ID)
    @RequireRoles(RoleConstant.CANDIDATE)
    public ResponseEntity<?> getResumeByJobId(@PathVariable("jobId") UUID jobId) {
        CandidateResumeResponse response = candidateResumeService.getResumeByJobId(jobId);
        return ResponseUtil.success("Get resume successfully", response);
    }
}
