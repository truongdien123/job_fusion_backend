package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(config = GlobalConfigMapper.class)
public interface TenantMapper {

    @Mapping(target = "planId", source = "tenant.plan.id")
    @Mapping(target = "planName", source = "tenant.plan.name")
    @Mapping(target = "maxUsers", source = "tenant.maxStaffAccount")
    TenantResponse toTenantResponse(Tenant tenant, UUID adminUserId, Long activeUsers, Long activeJob);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "plan", ignore = true)
    Tenant toEntity(TenantRequest request);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "plan", ignore = true)
    void updateTenant(TenantRequest request, @MappingTarget Tenant tenant);
}
