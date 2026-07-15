package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TenantService {
    TenantResponse createTenant(TenantRequest request);
    Page<TenantResponse> getListTenant(Pageable pageable);
    TenantResponse getTenantDetail(UUID id);
    TenantResponse updateTenant(UUID id, TenantRequest request);
    void deleteTenant(UUID id);
}
