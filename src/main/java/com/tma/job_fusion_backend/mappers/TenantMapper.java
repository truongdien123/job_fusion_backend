package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(config = GlobalConfigMapper.class)
public interface TenantMapper extends EntityMapper<TenantRequest, TenantResponse, Tenant> {

    @Mapping(target = "planId", source = "tenant.plan.id")
    @Mapping(target = "planName", source = "tenant.plan.name")
    @Mapping(target = "maxUsers", source = "tenant.maxStaffAccount")
    TenantResponse toResponse(Tenant tenant, UUID adminUserId, Long activeUsers, Long activeJob);

}
