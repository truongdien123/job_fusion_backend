package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.pojo.requests.JobPostingRequest;
import com.tma.job_fusion_backend.pojo.responses.JobPostingResponse;
import org.mapstruct.*;

@Mapper(config = GlobalConfigMapper.class)
public interface JobPostingMapper extends EntityMapper<JobPostingRequest, JobPostingResponse, JobPosting> {

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.companyName")
    JobPostingResponse toResponse(JobPosting jobPosting);
}
