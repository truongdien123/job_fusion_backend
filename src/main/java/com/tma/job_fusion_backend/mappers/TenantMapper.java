package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.requests.CreateTenantRequest;
import com.tma.job_fusion_backend.pojo.requests.UpdateTenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "planId", source = "tenant.plan.id")
    @Mapping(target = "adminUserId", source = "adminUserId")
    @Mapping(target = "id", source = "tenant.id")
    TenantResponse toTenantResponse(Tenant tenant, UUID adminUserId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "plan", ignore = true)
    Tenant toEntity(CreateTenantRequest request);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "plan", ignore = true)
    void updateTenant(UpdateTenantRequest request, @MappingTarget Tenant tenant);
}
