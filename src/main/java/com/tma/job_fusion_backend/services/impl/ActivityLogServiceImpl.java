package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.mappers.ActivityLogMapper;
import com.tma.job_fusion_backend.models.ActivityLog;
import com.tma.job_fusion_backend.pojo.dtos.ActivityLogFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.ActivityLogResponse;
import com.tma.job_fusion_backend.repositories.query.ActivityLogQueryRepository;
import com.tma.job_fusion_backend.services.ActivityLogService;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogQueryRepository activityLogQueryRepository;
    private final ActivityLogMapper activityLogMapper;
    private final ValidationUtil validationUtil;

    @Override
    public Page<ActivityLogResponse> getListActivityLog(PagingRequest<ActivityLogFilter> request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        ActivityLogFilter filter = (ObjectUtils.isNotEmpty(request) && ObjectUtils.isNotEmpty(request.getFilters()))
                ? request.getFilters()
                : new ActivityLogFilter();
        filter.setTenantId(tenantId);

        Pageable pageable = request.toPageable();
        Page<ActivityLog> logPage = activityLogQueryRepository.findAllActivityLogs(filter, pageable);

        return logPage.map(activityLogMapper::toResponse);
    }
}
