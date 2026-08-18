package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.JobCriteria;
import com.tma.job_fusion_backend.pojo.requests.JobCriteriaRequest;
import com.tma.job_fusion_backend.pojo.responses.JobCriteriaResponse;
import org.mapstruct.*;

@Mapper(config = GlobalConfigMapper.class)
public interface JobCriteriaMapper extends EntityMapper<JobCriteriaRequest, JobCriteriaResponse, JobCriteria> {

    @Mapping(target = "jobId", source = "job.id")
    JobCriteriaResponse toResponse(JobCriteria jobCriteria);
}
