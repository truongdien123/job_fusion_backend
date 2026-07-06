package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.CreateTenantRequest;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService {
    TenantResponse createTenant(CreateTenantRequest request);
    Page<TenantResponse> getListTenant(Pageable pageable);
}
