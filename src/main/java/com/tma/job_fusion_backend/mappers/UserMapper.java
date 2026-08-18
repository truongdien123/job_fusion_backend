package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.annotations.ToEntityMapping;
import com.tma.job_fusion_backend.pojo.requests.SignUpRequest;
import com.tma.job_fusion_backend.pojo.requests.StaffRequest;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalConfigMapper.class)
public interface UserMapper {

    @Mapping(source = "user.tenant.id", target = "tenantId")
    @Mapping(source = "user.tenant.plan.id", target = "planId")
    UserResponse toUserResponse(User user, String userRole);

    @ToEntityMapping
    User toEntity(SignUpRequest request);

    @ToEntityMapping
    User toEntity(StaffRequest request);
}
