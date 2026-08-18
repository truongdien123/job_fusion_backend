package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.pojo.dtos.AiHistoryFilter;
import com.tma.job_fusion_backend.pojo.responses.AiGenerationHistoryResponse;

public interface AiGenerationHistoryService {
    void saveHistory(String featureType, Object promptInput, Object generatedOutput);
    PageResponse<AiGenerationHistoryResponse> getHistoryList(PagingRequest<AiHistoryFilter> pagingRequest);
}
