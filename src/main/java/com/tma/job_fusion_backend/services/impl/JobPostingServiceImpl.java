package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
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
import com.tma.job_fusion_backend.repositories.ActivityLogRepository;
import com.tma.job_fusion_backend.repositories.query.JobPostingQueryRepository;
import com.tma.job_fusion_backend.repositories.query.CandidateApplicationQueryRepository;
import com.tma.job_fusion_backend.services.JobPostingService;
import com.tma.job_fusion_backend.services.ActivityLogService;
import com.tma.job_fusion_backend.enums.EventType;
import com.tma.job_fusion_backend.enums.JobPostingAction;
import com.tma.job_fusion_backend.models.ActivityLog;
import com.tma.job_fusion_backend.pojo.responses.JobPostingRevisionResponse;
import com.tma.job_fusion_backend.pojo.responses.TenantJobLimitResponse;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final ActivityLogRepository activityLogRepository;
    private final CandidateApplicationQueryRepository candidateApplicationQueryRepository;

    @Override
    @Transactional
    public JobPostingResponse createJobPosting(JobPostingRequest request) {
        UserPrincipal currentUser = getAndValidateUser();

        Tenant tenant = getTenantById(currentUser.getTenantId());

        validateTitleUniqueness(currentUser.getTenantId(), request.getTitle(), null);
        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        JobStatus targetStatus = ObjectUtils.isNotEmpty(request.getStatus()) ? request.getStatus() : JobStatus.OPEN;
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
                "Created job posting: " + savedJob.getTitle(),
                savedJob.getId(),
                JobPostingAction.CREATE
        );

        return toResponseWithRevisions(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobPostingResponse> getListJobPosting(PagingRequest<JobPostingFilter> request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        JobPostingFilter filter = ObjectUtils.isNotEmpty(request) ? request.getFilters() : null;
        if (currentUser.hasRole(RoleConstant.CANDIDATE)) {
            filter = ObjectUtils.isEmpty(filter) ? new JobPostingFilter() : filter;
            filter.setStatus(JobStatus.OPEN);
        }
        Page<JobPosting> jobPage = jobPostingQueryRepository.findAllJobPostings(currentUser.getTenantId(), filter, request.toPageable());
        Page<JobPostingResponse> mappedPage = jobPage.map(jobPostingMapper::toResponse);

        List<UUID> jobIds = jobPage.getContent().stream()
                .map(JobPosting::getId)
                .collect(Collectors.toList());

        if (ObjectUtils.isNotEmpty(jobIds)) {
            Map<UUID, Long> applicantCounts = candidateApplicationQueryRepository.countByJobIds(jobIds);
            mappedPage.getContent().forEach(response ->
                response.setNumberOfApplicant(applicantCounts.getOrDefault(response.getId(), 0L))
            );
        }

        return PageResponse.of(mappedPage);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponse getJobPostingDetail(UUID id) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(id);
        validateTenantAccess(currentUser, jobPosting);

        if (currentUser.hasRole(RoleConstant.CANDIDATE) && jobPosting.getStatus() == JobStatus.DRAFT) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        return toResponseWithRevisions(jobPosting);
    }

    @Override
    @Transactional
    public JobPostingResponse updateJobPosting(UUID id, JobPostingRequest request) {
        UserPrincipal currentUser = getAndValidateUser();
        JobPosting jobPosting = findJobPostingById(id);
        validateTenantAccess(currentUser, jobPosting);

        if (StringUtils.isNotEmpty(request.getTitle()) && !request.getTitle().trim().equalsIgnoreCase(jobPosting.getTitle())) {
            validateTitleUniqueness(currentUser.getTenantId(), request.getTitle(), id);
        }

        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());

        JobStatus targetStatus = request.getStatus() != null ? request.getStatus() : jobPosting.getStatus();
        if (targetStatus == JobStatus.OPEN && jobPosting.getStatus() != JobStatus.OPEN) {
            validateActiveJobPostingLimit(jobPosting.getTenant());
        }

        JobPostingAction action = JobPostingAction.UPDATE;
        String description = "Updated job posting: " + jobPosting.getTitle();
        if (targetStatus == JobStatus.OPEN && jobPosting.getStatus() != JobStatus.OPEN) {
            action = JobPostingAction.OPEN;
            description = "Opened job posting: " + jobPosting.getTitle();
        } else if (targetStatus == JobStatus.CLOSED && jobPosting.getStatus() != JobStatus.CLOSED) {
            action = JobPostingAction.CLOSE;
            description = "Closed job posting: " + jobPosting.getTitle();
        }

        jobPostingMapper.updateEntityFromRequest(request, jobPosting);
        jobPosting.setStatus(targetStatus);
        jobPosting.setUpdatedBy(currentUser.getId());

        JobPosting savedJob = jobPostingRepository.save(jobPosting);

        activityLogService.log(
                currentUser.getId(),
                EventType.ACTION,
                description,
                savedJob.getId(),
                action
        );

        return toResponseWithRevisions(savedJob);
    }

    @Override
    @Transactional
    public void deleteJobPosting(UUID id) {
        UserPrincipal currentUser = getAndValidateUser();
        JobPosting jobPosting = findJobPostingById(id);
        validateTenantAccess(currentUser, jobPosting);

        jobPosting.setDeletedAt(DateTimeUtil.nowUtc());
        jobPosting.setUpdatedBy(currentUser.getId());
        JobPosting savedJob = jobPostingRepository.save(jobPosting);

        activityLogService.log(
                currentUser.getId(),
                EventType.ACTION,
                "Deleted job posting: " + jobPosting.getTitle(),
                savedJob.getId(),
                JobPostingAction.DELETE
        );
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

    private Tenant getTenantById(UUID id) {
        return tenantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TENANT_NOT_FOUND));
    }

    private UserPrincipal getAndValidateUser() {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        UUID tenantId = currentUser.getTenantId();

        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
        return currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public void checkTitleUniqueness(String title, UUID excludeId) {
        UserPrincipal currentUser = getAndValidateUser();

        validateTitleUniqueness(currentUser.getTenantId(), title, excludeId);
    }

    private JobPostingResponse toResponseWithRevisions(JobPosting jobPosting) {
        JobPostingResponse response = jobPostingMapper.toResponse(jobPosting);
        response.setNumberOfApplicant(candidateApplicationQueryRepository.countByJobId(jobPosting.getId()));
        List<ActivityLog> logs = activityLogRepository.findAllByJobPostingIdAndDeletedAtIsNullOrderByCreatedAtDesc(jobPosting.getId());
        List<JobPostingRevisionResponse> revisions = logs.stream()
                .filter(log -> ObjectUtils.isNotEmpty(log.getAction()))
                .map(log -> {
                    JobPostingAction action = log.getAction();
                    String actionLabel = switch (action) {
                        case CREATE -> "Create Job Posting: \"" + jobPosting.getTitle() + "\"";
                        case UPDATE -> "Update Job Posting";
                        case OPEN -> "Open Job Posting";
                        case CLOSE -> "Closed Job Posting";
                        case DELETE -> "Deleted Job Posting";
                    };
                    return JobPostingRevisionResponse.builder()
                            .action(actionLabel)
                            .actorName(ObjectUtils.isNotEmpty(log.getUser()) ? log.getUser().getFullName() : null)
                            .createdAt(log.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        response.setRevisions(revisions);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TenantJobLimitResponse getTenantJobLimit() {
        UserPrincipal currentUser = getAndValidateUser();

        Tenant tenant = getTenantById(currentUser.getTenantId());

        long currentActiveJobs = jobPostingRepository.countByTenantIdAndStatusAndDeletedAtIsNull(tenant.getId(), JobStatus.OPEN);

        return TenantJobLimitResponse.builder()
                .currentActiveJobs(currentActiveJobs)
                .maxActiveJobs(tenant.getMaxActiveJobPosting())
                .build();
    }
}
