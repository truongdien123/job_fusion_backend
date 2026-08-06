package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.CandidateResume;
import com.tma.job_fusion_backend.pojo.responses.CandidateResumeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateResumeMapper {

    @Mapping(source = "user.id", target = "userId")
    CandidateResumeResponse toResponse(CandidateResume resume);
}
