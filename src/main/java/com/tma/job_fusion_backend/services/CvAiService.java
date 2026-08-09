package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.pojo.requests.CvEvaluateRequest;
import com.tma.job_fusion_backend.pojo.responses.CvEvaluateResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CvAiService {
    CvEvaluateResponse evaluateCv(
            MultipartFile file,
            List<CvEvaluateRequest.JobCriterionInput> criteria,
            String jobTitle,
            String jobDescription,
            String jobRequirements
    );
}
