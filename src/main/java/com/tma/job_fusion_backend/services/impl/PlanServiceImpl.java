package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.InvalidPlanException;
import com.tma.job_fusion_backend.mappers.PlanMapper;
import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.pojo.dtos.FeatureDto;
import com.tma.job_fusion_backend.pojo.requests.CreatePlanRequest;
import com.tma.job_fusion_backend.pojo.responses.PlanResponse;
import com.tma.job_fusion_backend.repositories.PlanRepository;
import com.tma.job_fusion_backend.services.PlanService;
import com.tma.job_fusion_backend.utils.JsonUtil;
import com.tma.job_fusion_backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        if ((!request.getActiveJobPostingUnlimited() && ObjectUtils.isEmpty(request.getMaxActiveJobPosting())) || (request.getActiveJobPostingUnlimited() && request.getMaxActiveJobPosting() != null)) {
            throw new InvalidPlanException(ErrorCode.INVALID_JOB_POSTING);
        }
        if ((!request.getStaffAccountUnlimited() && ObjectUtils.isEmpty(request.getMaxStaffAccount())) || (request.getStaffAccountUnlimited() && request.getMaxStaffAccount() != null)) {
            throw new InvalidPlanException(ErrorCode.INVALID_STAFF_ACCOUNT);
        }
        Plan plan = planMapper.toEntity(request);

        plan.setCreatedBy(jwtUtil.getCurrentUserId());

        if (request.getFeatures() != null && !CollectionUtils.isEmpty(request.getFeatures())) {
            plan.setFeature(JsonUtil.convertFeaturesToJson(request.getFeatures()));
        }
        
        planRepository.save(plan);

        PlanResponse response = planMapper.toPlanResponse(plan);
        response.setFeatures(JsonUtil.convertJsonToFeatures(plan.getFeature()));

        return response;
    }

    @Override
    public Page<PlanResponse> getListPlan(Pageable pageable) {
        return planRepository.findAll(pageable).map(plan -> {
            PlanResponse response = planMapper.toPlanResponse(plan);
            response.setFeatures(JsonUtil.convertJsonToFeatures(plan.getFeature()));
            return response;
        });
    }
}
