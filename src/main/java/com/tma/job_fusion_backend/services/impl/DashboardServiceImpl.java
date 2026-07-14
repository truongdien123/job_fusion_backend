package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.pojo.responses.DashboardStatsTenantResponse;
import com.tma.job_fusion_backend.repositories.query.TenantQueryRepository;
import com.tma.job_fusion_backend.services.DashboardService;
import lombok.RequiredArgsConstructor;
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
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .activeTenants(activeTenants != null ? activeTenants : 0L)
                .averageUsage(averageUsage != null ? averageUsage : 0.0)
                .churnRate(churnRate != null ? churnRate : 0.0)
                .build();
    }
}
