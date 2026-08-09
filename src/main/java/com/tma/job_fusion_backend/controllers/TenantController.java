package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import jakarta.validation.Valid;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.dtos.TenantFilter;
import com.tma.job_fusion_backend.pojo.requests.TenantRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.services.TenantService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_TENANT)
@RequiredArgsConstructor
@Tag(name = "tenant")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> createTenant(@Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseUtil.success("Create tenant successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles({RoleConstant.SUPER_ADMIN, RoleConstant.CANDIDATE})
    public ResponseEntity<?> getListTenant(@RequestBody PagingRequest<TenantFilter> request) {
        Pageable pageable = request.toPageable();
        TenantFilter filter = request.getFilters();
        Page<TenantResponse> listTenant = tenantService.getListTenant(filter, pageable);
        return ResponseUtil.success("Get tenants successfully", PageResponse.of(listTenant));
    }

    @GetMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.SUPER_ADMIN, RoleConstant.TENANT_ADMIN, RoleConstant.CANDIDATE})
    public ResponseEntity<?> getTenantDetail(@PathVariable UUID id) {
        TenantResponse response = tenantService.getTenantDetail(id);
        return ResponseUtil.success("Get tenant detail successfully", response);
    }

    @PutMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles({RoleConstant.SUPER_ADMIN, RoleConstant.TENANT_ADMIN})
    public ResponseEntity<?> updateTenant(@PathVariable UUID id, @Valid @RequestBody TenantRequest request) {
        TenantResponse response = tenantService.updateTenant(id, request);
        return ResponseUtil.success("Update tenant successfully", response);
    }

    @DeleteMapping(EndpointConstant.ENDPOINT_ID)
    @RequireRoles(RoleConstant.SUPER_ADMIN)
    public ResponseEntity<?> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseUtil.success("Delete tenant successfully", Boolean.TRUE);
    }
}
