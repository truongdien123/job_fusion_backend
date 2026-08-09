package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.CandidateApplicationFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.CandidateApplicationResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;

public interface CandidateApplicationService {
    PageResponse<CandidateApplicationResponse> getApplications(PagingRequest<CandidateApplicationFilter> request);
    void markAsReviewed(java.util.UUID id);
}
