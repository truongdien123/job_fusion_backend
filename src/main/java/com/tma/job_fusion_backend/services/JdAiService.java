package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.JdGenerateRequest;
import com.tma.job_fusion_backend.pojo.responses.JdGenerateResponse;

public interface JdAiService {
    JdGenerateResponse generateJd(JdGenerateRequest request);
}
