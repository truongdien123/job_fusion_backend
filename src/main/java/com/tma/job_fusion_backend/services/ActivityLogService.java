package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.dtos.ActivityLogFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.ActivityLogResponse;
import org.springframework.data.domain.Page;

public interface ActivityLogService {
    Page<ActivityLogResponse> getListActivityLog(PagingRequest<ActivityLogFilter> request);
}
