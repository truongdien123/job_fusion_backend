package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.enums.EventType;
import com.tma.job_fusion_backend.enums.JobPostingAction;
import com.tma.job_fusion_backend.pojo.dtos.ActivityLogFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.ActivityLogResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ActivityLogService {
    Page<ActivityLogResponse> getListActivityLog(PagingRequest<ActivityLogFilter> request);
    void log(UUID userId, EventType eventType, String description);
    void log(UUID userId, EventType eventType, String description, UUID jobPostingId, JobPostingAction action);
    void deleteAllActivityLog(UUID staffId);
}
