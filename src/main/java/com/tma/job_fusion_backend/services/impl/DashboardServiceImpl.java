package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.pojo.responses.DashboardStatsTenantResponse;
import com.tma.job_fusion_backend.repositories.query.TenantQueryRepository;
import com.tma.job_fusion_backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TenantQueryRepository tenantQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsTenantResponse getDashboardStatsTenant() {
        Double totalRevenue = tenantQueryRepository.calculateTotalRevenue();
        Long activeTenants = tenantQueryRepository.countActiveTenants();
        Double averageUsage = tenantQueryRepository.calculateAverageUsage();
        Double churnRate = tenantQueryRepository.calculateChurnRate();

        return DashboardStatsTenantResponse.builder()
                .totalRevenue(ObjectUtils.isNotEmpty(totalRevenue) ? totalRevenue : 0.0)
                .activeTenants(ObjectUtils.isNotEmpty(activeTenants) ? activeTenants : 0L)
                .averageUsage(ObjectUtils.isNotEmpty(averageUsage) ? averageUsage : 0.0)
                .churnRate(ObjectUtils.isNotEmpty(churnRate) ? churnRate : 0.0)
                .build();
    }
}
