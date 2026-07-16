package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.ActivityLog;
import com.tma.job_fusion_backend.pojo.responses.ActivityLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    ActivityLogResponse toResponse(ActivityLog activityLog);
}
