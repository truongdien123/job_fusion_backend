package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.models.CandidateApplication;
import com.tma.job_fusion_backend.models.CandidateResume;
import java.util.UUID;

public interface CvEvaluationService {
    void clearEvaluationAndMatchingResult(CandidateResume resume, CandidateApplication application);

    void asyncProcessResumeEvaluation(
            UUID resumeId,
            UUID applicationId,
            byte[] fileBytes,
            String originalFilename,
            String contentType,
            UUID jobId
    );
}
