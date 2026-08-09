package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.responses.CandidateResumeResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface CandidateResumeService {
    CandidateResumeResponse uploadResume(UUID jobId, MultipartFile file);
    CandidateResumeResponse getResumeByJobId(UUID jobId, UUID candidateId);
}
