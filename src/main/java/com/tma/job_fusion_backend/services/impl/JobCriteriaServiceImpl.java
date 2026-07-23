package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public JobCriteriaResponse createJobCriteria(JobCriteriaRequest request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        JobPosting jobPosting = findJobPostingById(request.getJobId());
        validateTenantAccess(currentUser, jobPosting);

        JobCriteria jobCriteria = jobCriteriaMapper.toEntity(request);
        jobCriteria.setJob(jobPosting);
        jobCriteria.setCreatedBy(currentUser.getId());

        JobCriteria saved = jobCriteriaRepository.save(jobCriteria);
        return jobCriteriaMapper.toResponse(saved);
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
