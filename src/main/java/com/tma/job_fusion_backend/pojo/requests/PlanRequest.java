package com.tma.job_fusion_backend.pojo.requests;

import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;

import java.util.List;

public interface PlanRequest {
    Boolean getActiveJobPostingUnlimited();
    Integer getMaxActiveJobPosting();
    Boolean getStaffAccountUnlimited();
    Integer getMaxStaffAccount();
    List<FeatureDto> getFeatures();
}
