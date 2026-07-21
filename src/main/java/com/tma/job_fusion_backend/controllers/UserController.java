package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.dtos.StaffFilter;
import jakarta.validation.Valid;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.StaffRequest;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.services.UserService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_USER)
@RequiredArgsConstructor
@Tag(name = "user")
public class UserController {

    private final UserService userService;

    @GetMapping(EndpointConstant.ENDPOINT_ID)
    public ResponseEntity<?> getUserDetail(@PathVariable UUID id) {
        UserResponse response = userService.getUserDetail(id);
        return ResponseUtil.success("Get user detail successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_STAFF_BASE)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> createStaff(@Valid @RequestBody StaffRequest request) {
        UserResponse response = userService.createStaff(request);
        return ResponseUtil.success("Create staff successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_STAFF_LIST)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> getListStaff(@RequestBody PagingRequest<StaffFilter> request) {
        PageResponse<UserResponse> response = userService.getListStaff(request);
        return ResponseUtil.success("Get list staff successfully", response);
    }

    @PutMapping(EndpointConstant.ENDPOINT_STAFF_ID)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> updateStaff(@PathVariable UUID id, @Valid @RequestBody StaffRequest request) {
        UserResponse response = userService.updateStaff(id, request);
        return ResponseUtil.success("Update staff successfully", response);
    }

    @DeleteMapping(EndpointConstant.ENDPOINT_STAFF_ID)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> deleteStaff(@PathVariable UUID id) {
        userService.deleteStaff(id);
        return ResponseUtil.success("Delete staff successfully", null);
    }

    @PostMapping(EndpointConstant.ENDPOINT_STAFF_RESEND_ACTIVATION)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> resendStaffActivation(@PathVariable UUID id) {
        userService.resendStaffActivation(id);
        return ResponseUtil.success("Resend staff activation link successfully", null);
    }
}
