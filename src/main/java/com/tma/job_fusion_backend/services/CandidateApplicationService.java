package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.CandidateApplicationFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.UpdateApplicationStatusRequest;
import com.tma.job_fusion_backend.pojo.responses.CandidateApplicationResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;

import java.util.UUID;

public interface CandidateApplicationService {
    PageResponse<CandidateApplicationResponse> getApplications(PagingRequest<CandidateApplicationFilter> request);
    void markAsReviewed(UUID id);
    void updateStatus(UUID id, UpdateApplicationStatusRequest request);
    CandidateApplicationResponse getApplicationDetail(UUID id);
}
