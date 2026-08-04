package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.JobPostingFilter;
import com.tma.job_fusion_backend.pojo.requests.JobPostingRequest;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.JobPostingResponse;
import com.tma.job_fusion_backend.pojo.responses.TenantJobLimitResponse;
import java.util.UUID;

public interface JobPostingService {
    JobPostingResponse createJobPosting(JobPostingRequest request);
    PageResponse<JobPostingResponse> getListJobPosting(PagingRequest<JobPostingFilter> request);
    JobPostingResponse getJobPostingDetail(UUID id);
    JobPostingResponse updateJobPosting(UUID id, JobPostingRequest request);
    void deleteJobPosting(UUID id);
    void checkTitleUniqueness(String title, UUID excludeId);
    TenantJobLimitResponse getTenantJobLimit();
}
