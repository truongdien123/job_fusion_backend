package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.dtos.AiHistoryFilter;
import com.tma.job_fusion_backend.pojo.responses.AiGenerationHistoryResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.services.AiGenerationHistoryService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_AI_HISTORY)
@RequiredArgsConstructor
@Tag(name = "ai-history")
public class AiGenerationHistoryController {

    private final AiGenerationHistoryService service;

    @PostMapping(EndpointConstant.ENDPOINT_LIST)
    @RequireRoles({RoleConstant.TENANT_ADMIN, RoleConstant.HR})
    public ResponseEntity<?> getHistoryList(@RequestBody PagingRequest<AiHistoryFilter> request) {
        PageResponse<AiGenerationHistoryResponse> response = service.getHistoryList(request);
        return ResponseUtil.success("Get AI generation history list successfully", response);
    }
}
