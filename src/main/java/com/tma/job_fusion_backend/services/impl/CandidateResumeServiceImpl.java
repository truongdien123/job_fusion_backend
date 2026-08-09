package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.BadRequestException;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.mappers.CandidateResumeMapper;
import com.tma.job_fusion_backend.enums.ApplicationStatus;
import com.tma.job_fusion_backend.models.CandidateApplication;
import com.tma.job_fusion_backend.models.CandidateResume;
import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.pojo.responses.CandidateResumeResponse;
import com.tma.job_fusion_backend.repositories.CandidateApplicationRepository;
import com.tma.job_fusion_backend.repositories.CandidateResumeRepository;
import com.tma.job_fusion_backend.repositories.JobPostingRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.services.CandidateResumeService;
import com.tma.job_fusion_backend.services.CvEvaluationService;
import com.tma.job_fusion_backend.services.FileStorageService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.JwtUtil;
import com.tma.job_fusion_backend.repositories.query.CandidateApplicationQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class CandidateResumeServiceImpl implements CandidateResumeService {

    private final CandidateResumeRepository candidateResumeRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CandidateResumeMapper candidateResumeMapper;
    private final JwtUtil jwtUtil;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateApplicationRepository candidateApplicationRepository;
    private final CvEvaluationService cvEvaluationService;
    private final CandidateApplicationQueryRepository candidateApplicationQueryRepository;

    @Override
    @Transactional
    public CandidateResumeResponse uploadResume(UUID jobId, MultipartFile file) {
        UUID currentUserId = getCurrentUserId();

        User user = getUserById(currentUserId);

        JobPosting jobPosting = getJobPostingById(jobId);

        // Upload the file to Cloudinary under the "candidate_resumes" folder
        String secureUrl = fileStorageService.uploadFile(file, "candidate_resumes");

        // Check if application already exists for this job posting
        Optional<CandidateApplication> existingAppOpt = candidateApplicationRepository
                .findByCandidateIdAndJobIdAndDeletedAtIsNull(currentUserId, jobId);

        CandidateResume resume;
        CandidateApplication application;
        if (existingAppOpt.isPresent()) {
            application = existingAppOpt.get();
            resume = application.getResume();
            resume.setFileUrl(secureUrl);
            resume.setUpdatedBy(currentUserId);
            
            // Reset old AI extraction results immediately
            resume.setParsedData(null);
            resume.setCandidateSelfScore(null);
            resume.setCvImprovementSuggestions(null);
            resume = candidateResumeRepository.save(resume);

            // Clean up old evaluation and matching data in the main transaction
            cvEvaluationService.clearEvaluationAndMatchingResult(resume, application);

            application.setAppliedAt(DateTimeUtil.nowUtc());
            application = candidateApplicationRepository.save(application);
            log.info("CV updated successfully for existing application of user: {}, job: {}, resume ID: {}", currentUserId, jobId, resume.getId());
        } else {
            resume = new CandidateResume();
            resume.setUser(user);
            resume.setFileUrl(secureUrl);
            resume.setCreatedBy(currentUserId);
            resume.setParsedData(null);
            resume.setCandidateSelfScore(null);
            resume.setCvImprovementSuggestions(null);
            resume = candidateResumeRepository.save(resume);

            application = new CandidateApplication();
            application.setJob(jobPosting);
            application.setCandidate(user);
            application.setResume(resume);
            application.setStatus(ApplicationStatus.APPLIED);
            application.setAppliedAt(DateTimeUtil.nowUtc());
            application = candidateApplicationRepository.save(application);
            log.info("CV uploaded and new application created for user: {}, job: {}, resume ID: {}", currentUserId, jobId, resume.getId());
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read file bytes: {}", e.getMessage());
            fileBytes = new byte[0];
        }

        final byte[] finalFileBytes = fileBytes;
        final String originalFilename = file.getOriginalFilename();
        final String contentType = file.getContentType();
        final UUID resumeId = resume.getId();
        final UUID applicationId = application.getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        cvEvaluationService.asyncProcessResumeEvaluation(
                                resumeId,
                                applicationId,
                                finalFileBytes,
                                originalFilename,
                                contentType,
                                jobId
                        );
                    } catch (Exception e) {
                        log.error("Async resume evaluation failed", e);
                    }
                });
            }
        });

        return candidateResumeMapper.toResponse(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResumeResponse getResumeByJobId(UUID jobId) {
        UUID currentUserId = getCurrentUserId();

        CandidateApplication application = candidateApplicationQueryRepository
                .findByCandidateIdAndJobIdWithResume(currentUserId, jobId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));

        CandidateResume resume = application.getResume();
        if (ObjectUtils.isEmpty(resume) || ObjectUtils.isNotEmpty(resume.getDeletedAt())) {
            throw new NotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }

        return candidateResumeMapper.toResponse(resume);
    }

    private UUID getCurrentUserId() {
        return Optional.ofNullable(jwtUtil.getCurrentUserId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
    }

    private User getUserById(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private JobPosting getJobPostingById(UUID id) {
        return jobPostingRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.JOB_POSTING_NOT_FOUND));
    }
}
