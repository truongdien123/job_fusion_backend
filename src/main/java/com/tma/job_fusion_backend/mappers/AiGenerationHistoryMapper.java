package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.AiGenerationHistory;
import com.tma.job_fusion_backend.pojo.responses.AiGenerationHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalConfigMapper.class)
public interface AiGenerationHistoryMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    AiGenerationHistoryResponse toResponse(AiGenerationHistory history);
}
