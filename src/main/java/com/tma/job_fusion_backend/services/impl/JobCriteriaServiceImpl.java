package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.mappers.JobCriteriaMapper;
import com.tma.job_fusion_backend.models.JobCriteria;
import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.pojo.requests.JobCriteriaRequest;
import com.tma.job_fusion_backend.pojo.responses.JobCriteriaResponse;
import com.tma.job_fusion_backend.repositories.JobCriteriaRepository;
import com.tma.job_fusion_backend.repositories.JobPostingRepository;
import com.tma.job_fusion_backend.services.JobCriteriaService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import com.tma.job_fusion_backend.enums.EventType;
import com.tma.job_fusion_backend.enums.JobPostingAction;
import com.tma.job_fusion_backend.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobCriteriaServiceImpl implements JobCriteriaService {

    private final JobCriteriaRepository jobCriteriaRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobCriteriaMapper jobCriteriaMapper;
    private final ValidationUtil validationUtil;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public List<JobCriteriaResponse> createJobCriteria(List<JobCriteriaRequest> requests) {
        if (ObjectUtils.isEmpty(requests)) {
            throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
        }

        UUID jobId = requests.getFirst().getJobId();
        if (ObjectUtils.isEmpty(jobId)) {
            throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
        }

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(jobId);
        validateTenantAccess(currentUser, jobPosting);

        // Fetch existing active criteria for this jobId
        List<JobCriteria> existing = jobCriteriaRepository.findByJobIdAndDeletedAtIsNull(jobId);

        // Put existing in a Map for quick lookup
        Map<UUID, JobCriteria> existingMap = existing.stream()
                .collect(Collectors.toMap(JobCriteria::getId, Function.identity()));

        double totalWeight = 0.0;
        Set<String> names = new HashSet<>();
        List<JobCriteria> jobCriteriaList = new ArrayList<>();
        Set<UUID> processedIds = new HashSet<>();

        // Combined validation and processing loop
        for (JobCriteriaRequest req : requests) {
            if (!jobId.equals(req.getJobId())) {
                throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
            }
            if (StringUtils.isEmpty(req.getCriterionName())) {
                throw new BadRequestException(ErrorCode.INVALID_CRITERION_NAME);
            }
            if (ObjectUtils.isEmpty(req.getWeight()) || req.getWeight() <= 0.0) {
                throw new BadRequestException(ErrorCode.INVALID_CRITERION_WEIGHT);
            }

            String normalizedName = req.getCriterionName().trim().toLowerCase();
            if (!names.add(normalizedName)) {
                throw new BadRequestException(ErrorCode.DUPLICATE_CRITERION_NAME);
            }

            totalWeight += req.getWeight();

            if (ObjectUtils.isNotEmpty(req.getId())) {
                JobCriteria jobCriteria = existingMap.get(req.getId());
                if (ObjectUtils.isEmpty(jobCriteria)) {
                    throw new BadRequestException(ErrorCode.JOB_CRITERIA_NOT_FOUND);
                }
                jobCriteriaMapper.updateEntityFromRequest(req, jobCriteria);
                jobCriteria.setUpdatedBy(currentUser.getId());
                jobCriteriaList.add(jobCriteria);
                processedIds.add(req.getId());
            } else {
                JobCriteria jobCriteria = jobCriteriaMapper.toEntity(req);
                jobCriteria.setJob(jobPosting);
                jobCriteria.setCreatedBy(currentUser.getId());
                jobCriteriaList.add(jobCriteria);
            }
        }

        // Total weight must be exactly 100
        if (Math.abs(totalWeight - 100.0) > 1e-9) {
            throw new BadRequestException(ErrorCode.INVALID_TOTAL_WEIGHT);
        }

        // Soft delete existing criteria that are not present in requests
        LocalDateTime now = DateTimeUtil.nowUtc();
        existing.stream()
                .filter(jobCriteria -> !processedIds.contains(jobCriteria.getId()))
                .forEach(jobCriteria -> {
                    jobCriteria.setDeletedAt(now);
                    jobCriteria.setUpdatedBy(currentUser.getId());
                    jobCriteriaList.add(jobCriteria);
                });

        // Save all changes
        List<JobCriteria> savedList = jobCriteriaRepository.saveAll(jobCriteriaList);

        activityLogService.log(
                currentUser.getId(),
                EventType.ACTION,
                "Updated job criteria for job: " + jobPosting.getTitle(),
                jobPosting.getId(),
                JobPostingAction.UPDATE
        );

        // Filter and return only the active (non-deleted) criteria
        return savedList.stream()
                .filter(jobCriteria -> ObjectUtils.isEmpty(jobCriteria.getDeletedAt()))
                .map(jobCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobCriteriaResponse getJobCriteriaDetail(UUID id) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobCriteria jobCriteria = findJobCriteriaById(id);
        validateTenantAccess(currentUser, jobCriteria.getJob());

        return jobCriteriaMapper.toResponse(jobCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobCriteriaResponse> getJobCriteriaByJobId(UUID jobId) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(jobId);
        validateTenantAccess(currentUser, jobPosting);

        List<JobCriteria> criteriaList = jobCriteriaRepository.findByJobIdAndDeletedAtIsNull(jobId);
        return criteriaList.stream()
                .map(jobCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobCriteriaResponse updateJobCriteria(UUID id, JobCriteriaRequest request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobCriteria jobCriteria = findJobCriteriaById(id);
        validateTenantAccess(currentUser, jobCriteria.getJob());

        // If the request modifies the jobId, validate the new job posting
        if (!jobCriteria.getJob().getId().equals(request.getJobId())) {
            JobPosting newJobPosting = findJobPostingById(request.getJobId());
            validateTenantAccess(currentUser, newJobPosting);
            jobCriteria.setJob(newJobPosting);
        }

        jobCriteriaMapper.updateEntityFromRequest(request, jobCriteria);
        jobCriteria.setUpdatedBy(currentUser.getId());

        JobCriteria saved = jobCriteriaRepository.save(jobCriteria);

        activityLogService.log(
                currentUser.getId(),
                EventType.ACTION,
                "Updated job criterion: " + saved.getCriterionName() + " for job: " + saved.getJob().getTitle(),
                saved.getJob().getId(),
                JobPostingAction.UPDATE
        );

        return jobCriteriaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteJobCriteria(UUID id) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobCriteria jobCriteria = findJobCriteriaById(id);
        validateTenantAccess(currentUser, jobCriteria.getJob());

        jobCriteria.setDeletedAt(DateTimeUtil.nowUtc());
        jobCriteria.setUpdatedBy(currentUser.getId());
        jobCriteriaRepository.save(jobCriteria);
    }

    @Override
    @Transactional
    public void deleteAllJobCriteriaByJobId(UUID jobId) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(jobId);
        validateTenantAccess(currentUser, jobPosting);

        List<JobCriteria> criteriaList = jobCriteriaRepository.findByJobIdAndDeletedAtIsNull(jobId);
        if (ObjectUtils.isNotEmpty(criteriaList)) {
            LocalDateTime now = DateTimeUtil.nowUtc();
            for (JobCriteria jc : criteriaList) {
                jc.setDeletedAt(now);
                jc.setUpdatedBy(currentUser.getId());
            }
            jobCriteriaRepository.saveAll(criteriaList);
        }
    }

    private JobPosting findJobPostingById(UUID jobId) {
        return jobPostingRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_POSTING_NOT_FOUND));
    }

    private JobCriteria findJobCriteriaById(UUID id) {
        return jobCriteriaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_CRITERIA_NOT_FOUND));
    }

    private void validateTenantAccess(UserPrincipal currentUser, JobPosting jobPosting) {
        if (ObjectUtils.isEmpty(jobPosting.getTenant()) || !currentUser.getTenantId().equals(jobPosting.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
    }

}
