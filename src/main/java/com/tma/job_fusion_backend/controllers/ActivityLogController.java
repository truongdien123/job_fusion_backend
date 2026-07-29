package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.dtos.ActivityLogFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.ActivityLogResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.services.ActivityLogService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_ACTIVITY_LOG)
@RequiredArgsConstructor
@Tag(name = "activity-log")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> getListActivityLog(@RequestBody PagingRequest<ActivityLogFilter> request) {
        Page<ActivityLogResponse> result = activityLogService.getListActivityLog(request);
        PageResponse<ActivityLogResponse> response = PageResponse.of(result);
        return ResponseUtil.success("Get list activity log successfully", response);
    }

    @DeleteMapping(EndpointConstant.ENDPOINT_STAFF_ID)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<?> deleteAllActivityLog(@PathVariable UUID id) {
        activityLogService.deleteAllActivityLog(id);
        return ResponseUtil.success("Delete all activity logs of staff successfully", Boolean.TRUE);
    }

    @GetMapping(EndpointConstant.ENDPOINT_STAFF_EXPORT)
    @RequireRoles(RoleConstant.TENANT_ADMIN)
    public ResponseEntity<byte[]> exportActivityLog(@PathVariable UUID id) {
        byte[] data = activityLogService.exportActivityLogToExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"activity_log_" + id + ".xlsx\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(data);
    }
}
