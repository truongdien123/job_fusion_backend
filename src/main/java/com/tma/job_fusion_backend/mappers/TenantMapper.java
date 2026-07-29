package com.tma.job_fusion_backend.mappers;

import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "planId", source = "tenant.plan.id")
    @Mapping(target = "planName", source = "tenant.plan.name")
    @Mapping(target = "maxUsers", source = "tenant.maxStaffAccount")
    @Mapping(target = "adminUserId", source = "adminUserId")
    @Mapping(target = "id", source = "tenant.id")
    @Mapping(target = "activeUsers", source = "activeUsers")
    @Mapping(target = "price", source = "tenant.price")
    @Mapping(target = "billingCycle", source = "tenant.billingCycle")
    @Mapping(target = "activeJob", source = "activeJob")
    @Mapping(target = "maxActiveJobPosting", source = "tenant.maxActiveJobPosting")
    TenantResponse toTenantResponse(Tenant tenant, UUID adminUserId, Long activeUsers, Long activeJob);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
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
