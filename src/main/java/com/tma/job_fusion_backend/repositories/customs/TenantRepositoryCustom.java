package com.tma.job_fusion_backend.repositories.customs;

import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantRepositoryCustom {
    Page<TenantResponse> findAllActiveTenants(Pageable pageable);
}
