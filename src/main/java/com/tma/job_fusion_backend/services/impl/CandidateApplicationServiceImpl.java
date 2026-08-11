package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.models.CandidateApplication;
import com.tma.job_fusion_backend.models.UserRole;
import com.tma.job_fusion_backend.pojo.dtos.CandidateApplicationFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.requests.UpdateApplicationStatusRequest;
import com.tma.job_fusion_backend.pojo.responses.CandidateApplicationResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.repositories.CandidateApplicationRepository;
import com.tma.job_fusion_backend.repositories.query.CandidateApplicationQueryRepository;
import com.tma.job_fusion_backend.services.CandidateApplicationService;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateApplicationServiceImpl implements CandidateApplicationService {

    private final CandidateApplicationQueryRepository queryRepository;
    private final CandidateApplicationRepository candidateApplicationRepository;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CandidateApplicationResponse> getApplications(PagingRequest<CandidateApplicationFilter> request) {
        UserPrincipal currentUser = getCurrentUser();

        Pageable pageable = request.toPageable();
        CandidateApplicationFilter filter = request.getFilters();
        Page<CandidateApplicationResponse> page = queryRepository.findApplicationsByTenant(currentUser.getTenantId(), filter, pageable);

        return PageResponse.of(page);
    }

    @Override
    @Transactional
    public void markAsReviewed(UUID id) {
        UserPrincipal currentUser = getCurrentUser();

        CandidateApplication application = candidateApplicationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        if (ObjectUtils.isEmpty(application.getJob()) || 
            ObjectUtils.isEmpty(application.getJob().getTenant()) || 
            !currentUser.getTenantId().equals(application.getJob().getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        application.setReviewed(true);
        application.setUpdatedBy(currentUser.getId());
        candidateApplicationRepository.save(application);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, UpdateApplicationStatusRequest request) {
        UserPrincipal currentUser = getCurrentUser();

        CandidateApplication application = candidateApplicationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.APPLICATION_NOT_FOUND));

        if (ObjectUtils.isEmpty(application.getJob()) || 
            ObjectUtils.isEmpty(application.getJob().getTenant()) || 
            !currentUser.getTenantId().equals(application.getJob().getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        application.setStatus(request.getStatus());
        application.setUpdatedBy(currentUser.getId());
        candidateApplicationRepository.save(application);
    }

    private UserPrincipal getCurrentUser() {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        if (ObjectUtils.isEmpty(currentUser.getTenantId()) || (!currentUser.hasRole(RoleConstant.HR))) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
        return currentUser;
    }
}
