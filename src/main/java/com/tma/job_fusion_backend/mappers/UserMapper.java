package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.annotations.ToEntityMapping;
import com.tma.job_fusion_backend.pojo.requests.SignUpRequest;
import com.tma.job_fusion_backend.pojo.requests.StaffRequest;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "tenant.id", target = "tenantId")
    UserResponse toUserResponse(User user);

    @ToEntityMapping
    User toEntity(SignUpRequest request);

    @ToEntityMapping
    User toEntity(StaffRequest request);
}
