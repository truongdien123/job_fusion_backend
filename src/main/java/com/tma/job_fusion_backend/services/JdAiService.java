package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.JdGenerateRequest;
import com.tma.job_fusion_backend.pojo.responses.JdGenerateResponse;
import com.tma.job_fusion_backend.pojo.requests.CriteriaAiGenerateRequest;
import com.tma.job_fusion_backend.pojo.responses.CriteriaAiGenerateResponse;

import java.util.List;

public interface JdAiService {
    JdGenerateResponse generateJd(JdGenerateRequest request);
    CriteriaAiGenerateResponse generateJobCriteria(CriteriaAiGenerateRequest request);
}

