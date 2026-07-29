package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.pojo.requests.JobPostingRequest;
import com.tma.job_fusion_backend.pojo.responses.JobPostingResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobPostingMapper {

    JobPosting toEntity(JobPostingRequest request);

    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.companyName")
    @Mapping(target = "revisions", ignore = true)
    JobPostingResponse toResponse(JobPosting jobPosting);

    void updateEntityFromRequest(JobPostingRequest request, @MappingTarget JobPosting jobPosting);
}
