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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobCriteriaServiceImpl implements JobCriteriaService {

    private final JobCriteriaRepository jobCriteriaRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobCriteriaMapper jobCriteriaMapper;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional
    public List<JobCriteriaResponse> createJobCriteria(List<JobCriteriaRequest> requests) {
        if (ObjectUtils.isEmpty(requests)) {
            throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
        }

        UUID jobId = requests.get(0).getJobId();
        if (ObjectUtils.isEmpty(jobId)) {
            throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
        }

        // Validate all requests belong to the same jobId
        for (JobCriteriaRequest req : requests) {
            if (!jobId.equals(req.getJobId())) {
                throw new BadRequestException(ErrorCode.INVALID_JOB_CRITERIA);
            }
        }

        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(jobId);
        validateTenantAccess(currentUser, jobPosting);

        // Validation checks
        double totalWeight = 0.0;
        Set<String> names = new HashSet<>();

        for (JobCriteriaRequest req : requests) {
            if (req.getCriterionName() == null || req.getCriterionName().trim().isEmpty()) {
                throw new BadRequestException(ErrorCode.INVALID_CRITERION_NAME);
            }
            if (req.getWeight() == null || req.getWeight() <= 0.0) {
                throw new BadRequestException(ErrorCode.INVALID_CRITERION_WEIGHT);
            }

            String normalizedName = req.getCriterionName().trim().toLowerCase();
            if (!names.add(normalizedName)) {
                throw new BadRequestException(ErrorCode.DUPLICATE_CRITERION_NAME);
            }

            totalWeight += req.getWeight();
        }

        // Total weight must be exactly 100
        if (Math.abs(totalWeight - 100.0) > 1e-9) {
            throw new BadRequestException(ErrorCode.INVALID_TOTAL_WEIGHT);
        }

        // Soft delete all existing criteria for this jobId
        List<JobCriteria> existing = jobCriteriaRepository.findByJobIdAndDeletedAtIsNull(jobId);
        if (!existing.isEmpty()) {
            java.time.LocalDateTime now = DateTimeUtil.nowUtc();
            for (JobCriteria ec : existing) {
                ec.setDeletedAt(now);
                ec.setUpdatedBy(currentUser.getId());
            }
            jobCriteriaRepository.saveAll(existing);
        }

        // Save new criteria
        List<JobCriteria> newCriteriaList = requests.stream()
                .map(req -> {
                    JobCriteria jc = jobCriteriaMapper.toEntity(req);
                    jc.setJob(jobPosting);
                    jc.setCreatedBy(currentUser.getId());
                    return jc;
                })
                .collect(Collectors.toList());

        List<JobCriteria> savedList = jobCriteriaRepository.saveAll(newCriteriaList);

        return savedList.stream()
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

    private JobPosting findJobPostingById(UUID jobId) {
        return jobPostingRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_POSTING_NOT_FOUND));
    }

    private JobCriteria findJobCriteriaById(UUID id) {
        return jobCriteriaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_CRITERIA_NOT_FOUND));
    }

    private void validateTenantAccess(UserPrincipal currentUser, JobPosting jobPosting) {
        if (jobPosting.getTenant() == null || !currentUser.getTenantId().equals(jobPosting.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
    }

}
