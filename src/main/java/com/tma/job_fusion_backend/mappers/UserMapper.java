package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.pojo.requests.SignUpRequest;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "tenant.id", target = "tenantId")
    UserResponse toUserResponse(User user);

    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    User toEntity(SignUpRequest request);
}
