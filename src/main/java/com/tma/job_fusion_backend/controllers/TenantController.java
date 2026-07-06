package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.requests.CreateTenantRequest;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.services.TenantService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_TENANT)
@RequiredArgsConstructor
@Tag(name = "tenant")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseUtil.success("Create tenant successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> getListTenant(@RequestBody PagingRequest<?> request) {
        Pageable pageable = request.toPageable();
        Page<TenantResponse> listTenant = tenantService.getListTenant(pageable);
        return ResponseUtil.success("Get tenants successfully", PageResponse.of(listTenant));
    }
}
