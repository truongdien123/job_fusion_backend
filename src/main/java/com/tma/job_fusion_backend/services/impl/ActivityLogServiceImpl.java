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
import com.tma.job_fusion_backend.repositories.ActivityLogRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.services.ActivityLogService;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import com.tma.job_fusion_backend.enums.EventType;
import com.tma.job_fusion_backend.models.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogQueryRepository activityLogQueryRepository;
    private final ActivityLogMapper activityLogMapper;
    private final ValidationUtil validationUtil;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

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

        Pageable pageable = request.toPageable();
        Page<ActivityLog> logPage = activityLogQueryRepository.findAllActivityLogs(filter, pageable);

        return logPage.map(activityLogMapper::toResponse);
    }

    @Override
    @Transactional
    public void log(UUID userId, EventType eventType, String description) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
        if (ObjectUtils.isEmpty(user)) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setEventType(eventType);
        log.setDescription(description);
        log.setIpAddress(getClientIp());
        log.setCreatedBy(userId);

        activityLogRepository.save(log);
    }

    private String getClientIp() {
        String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
        };

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    @Override
    @Transactional
    public void deleteAllActivityLog(UUID staffId) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        User staff = userRepository.findByIdAndDeletedAtIsNull(staffId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (ObjectUtils.isEmpty(staff.getTenant()) || !tenantId.equals(staff.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        LocalDateTime now = DateTimeUtil.nowUtc();
        activityLogQueryRepository.softDeleteAllByUserId(staffId, now, currentUser.getId());
    }
}
