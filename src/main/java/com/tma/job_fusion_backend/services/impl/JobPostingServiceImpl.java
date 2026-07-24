package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.mappers.JobPostingMapper;
import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.dtos.JobPostingFilter;
import com.tma.job_fusion_backend.pojo.requests.JobPostingRequest;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.JobPostingResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.repositories.JobPostingRepository;
import com.tma.job_fusion_backend.repositories.TenantRepository;
import com.tma.job_fusion_backend.repositories.query.JobPostingQueryRepository;
import com.tma.job_fusion_backend.services.JobPostingService;
import com.tma.job_fusion_backend.services.ActivityLogService;
import com.tma.job_fusion_backend.enums.EventType;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingQueryRepository jobPostingQueryRepository;
    private final TenantRepository tenantRepository;
    private final JobPostingMapper jobPostingMapper;
    private final ValidationUtil validationUtil;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public JobPostingResponse createJobPosting(JobPostingRequest request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        Tenant tenant = tenantRepository.findByIdAndDeletedAtIsNull(tenantId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TENANT_NOT_FOUND));

        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        JobStatus targetStatus = request.getStatus() != null ? request.getStatus() : JobStatus.OPEN;
        if (JobStatus.OPEN == targetStatus) {
            validateActiveJobPostingLimit(tenant);
        }

        JobPosting jobPosting = jobPostingMapper.toEntity(request);
        jobPosting.setTenant(tenant);
        jobPosting.setStatus(targetStatus);
        jobPosting.setCreatedBy(currentUser.getId());

        JobPosting savedJob = jobPostingRepository.save(jobPosting);

        activityLogService.log(
                currentUser.getId(),
                EventType.ACTION,
                "Created job posting: " + savedJob.getTitle()
        );

        return jobPostingMapper.toResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobPostingResponse> getListJobPosting(PagingRequest<JobPostingFilter> request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        JobPostingFilter filter = ObjectUtils.isNotEmpty(request) ? request.getFilters() : null;
        Page<JobPosting> jobPage = jobPostingQueryRepository.findAllJobPostings(tenantId, filter, request.toPageable());
        Page<JobPostingResponse> mappedPage = jobPage.map(jobPostingMapper::toResponse);

        return PageResponse.of(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponse getJobPostingDetail(UUID id) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(id);
        validateTenantAccess(currentUser, jobPosting);

        return jobPostingMapper.toResponse(jobPosting);
    }

    @Override
    @Transactional
    public JobPostingResponse updateJobPosting(UUID id, JobPostingRequest request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(id);
        validateTenantAccess(currentUser, jobPosting);

        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        JobStatus targetStatus = request.getStatus() != null ? request.getStatus() : jobPosting.getStatus();
        if (targetStatus == JobStatus.OPEN && jobPosting.getStatus() != JobStatus.OPEN) {
            validateActiveJobPostingLimit(jobPosting.getTenant());
        }

        jobPostingMapper.updateEntityFromRequest(request, jobPosting);
        jobPosting.setStatus(targetStatus);
        jobPosting.setUpdatedBy(currentUser.getId());

        JobPosting savedJob = jobPostingRepository.save(jobPosting);
        return jobPostingMapper.toResponse(savedJob);
    }

    @Override
    @Transactional
    public void deleteJobPosting(UUID id) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(id);
        validateTenantAccess(currentUser, jobPosting);

        jobPosting.setDeletedAt(DateTimeUtil.nowUtc());
        jobPosting.setUpdatedBy(currentUser.getId());
        jobPostingRepository.save(jobPosting);
    }

    private JobPosting findJobPostingById(UUID id) {
        return jobPostingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_POSTING_NOT_FOUND));
    }

    private void validateTenantAccess(UserPrincipal currentUser, JobPosting jobPosting) {
        if (ObjectUtils.isNotEmpty(currentUser.getTenantId()) &&
                (ObjectUtils.isEmpty(jobPosting.getTenant()) || !currentUser.getTenantId().equals(jobPosting.getTenant().getId()))) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateSalaryRange(Double salaryMin, Double salaryMax) {
        if (ObjectUtils.isNotEmpty(salaryMin) && ObjectUtils.isNotEmpty(salaryMax) && salaryMax < salaryMin) {
            throw new BadRequestException(ErrorCode.INVALID_SALARY_RANGE);
        }
    }

    private void validateActiveJobPostingLimit(Tenant tenant) {
        if (ObjectUtils.isNotEmpty(tenant) && ObjectUtils.isNotEmpty(tenant.getMaxActiveJobPosting())) {
            long activeJobs = jobPostingRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenant.getId(), JobStatus.OPEN);
            if (activeJobs >= tenant.getMaxActiveJobPosting()) {
                throw new BadRequestException(ErrorCode.MAX_JOB_POSTING_LIMIT_REACHED);
            }
        }
    }

    private void validateTitleUniqueness(UUID tenantId, String title, UUID excludeId) {
        if (StringUtils.isEmpty(title)) {
            return;
        }
        String trimmedTitle = title.trim();
        boolean exists = (ObjectUtils.isEmpty(excludeId))
                ? jobPostingRepository.existsByTenantIdAndTitleIgnoreCaseAndDeletedAtIsNull(tenantId, trimmedTitle)
                : jobPostingRepository.existsByTenantIdAndTitleIgnoreCaseAndIdNotAndDeletedAtIsNull(tenantId, trimmedTitle, excludeId);

        if (exists) {
            throw new BadRequestException(ErrorCode.JOB_TITLE_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void checkTitleUniqueness(String title, UUID excludeId) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        validateTitleUniqueness(tenantId, title, excludeId);
    }
}
