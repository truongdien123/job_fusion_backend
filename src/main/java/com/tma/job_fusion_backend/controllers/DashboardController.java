package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsTenantResponse;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsPlanResponse;
import com.tma.job_fusion_backend.services.DashboardService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_DASHBOARD)
@RequiredArgsConstructor
@Tag(name = "dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping(EndpointConstant.ENDPOINT_STATS_TENANT)
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> getDashboardStats() {
        DashboardStatsTenantResponse response = dashboardService.getDashboardStatsTenant();
        return ResponseUtil.success("Get dashboard stats successfully", response);
    }

    @GetMapping(EndpointConstant.ENDPOINT_STATS_PLAN)
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> getDashboardStatsPlan() {
        DashboardStatsPlanResponse response = dashboardService.getDashboardStatsPlan();
        return ResponseUtil.success("Get plan dashboard stats successfully", response);
    }
}
