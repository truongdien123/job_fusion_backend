package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.JobCriteriaRequest;
import com.tma.job_fusion_backend.pojo.responses.JobCriteriaResponse;

import java.util.List;
import java.util.UUID;

public interface JobCriteriaService {

    JobCriteriaResponse createJobCriteria(JobCriteriaRequest request);

    JobCriteriaResponse getJobCriteriaDetail(UUID id);

    List<JobCriteriaResponse> getJobCriteriaByJobId(UUID jobId);

    JobCriteriaResponse updateJobCriteria(UUID id, JobCriteriaRequest request);

    void deleteJobCriteria(UUID id);

}
