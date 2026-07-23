package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.JobCriteria;
import com.tma.job_fusion_backend.pojo.requests.JobCriteriaRequest;
import com.tma.job_fusion_backend.pojo.responses.JobCriteriaResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobCriteriaMapper {

    JobCriteria toEntity(JobCriteriaRequest request);

    @Mapping(target = "jobId", source = "job.id")
    JobCriteriaResponse toResponse(JobCriteria jobCriteria);

    void updateEntityFromRequest(JobCriteriaRequest request, @MappingTarget JobCriteria jobCriteria);
}
