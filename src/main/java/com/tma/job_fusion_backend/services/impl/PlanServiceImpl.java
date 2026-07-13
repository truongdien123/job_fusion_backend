package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.exceptions.InvalidPlanException;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.mappers.PlanMapper;
import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.pojo.requests.PlanRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        validatePlan(request);
        Plan plan = planMapper.toEntity(request);

        plan.setCreatedBy(jwtUtil.getCurrentUserId());

        convertJson(plan, request);

        planRepository.save(plan);

        return buildResponse(plan);
    }

    @Override
    public Page<PlanResponse> getListPlan(Pageable pageable) {
        return planRepository.findAll(pageable).map(this::buildResponse);
    }

    @Override
    public PlanResponse getPlanDetail(UUID id) {
        Plan plan = findPlanById(id);
        return buildResponse(plan);
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(UUID id, PlanRequest request) {
        Plan plan = findPlanById(id);
        validatePlan(request);

        planMapper.updatePlan(request, plan);

        plan.setUpdatedBy(jwtUtil.getCurrentUserId());

        convertJson(plan, request);

        planRepository.save(plan);

        return buildResponse(plan);
    }

    private Plan findPlanById(UUID id) {
        return planRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new NotFoundException(ErrorCode.PLAN_NOT_FOUND));
    }

    private void convertJson(Plan plan, PlanRequest request) {
        if (ObjectUtils.isNotEmpty(request.getFeatures())) {
            plan.setFeature(JsonUtil.convertFeaturesToJson(request.getFeatures()));
        }
    }

    private void validatePlan(PlanRequest request) {
        if (ObjectUtils.isNotEmpty(request.getActiveJobPostingUnlimited()) || ObjectUtils.isNotEmpty(request.getMaxActiveJobPosting())) {
            boolean activeJobPostingUnlimited = Boolean.TRUE.equals(request.getActiveJobPostingUnlimited());
            if ((!activeJobPostingUnlimited && ObjectUtils.isEmpty(request.getMaxActiveJobPosting()))
                    || (activeJobPostingUnlimited && ObjectUtils.isNotEmpty(request.getMaxActiveJobPosting()))) {
                throw new InvalidPlanException(ErrorCode.INVALID_JOB_POSTING);
            }
        }
        if (ObjectUtils.isNotEmpty(request.getStaffAccountUnlimited()) || ObjectUtils.isNotEmpty(request.getMaxStaffAccount())) {
            boolean staffAccountUnlimited = Boolean.TRUE.equals(request.getStaffAccountUnlimited());
            if ((!staffAccountUnlimited && ObjectUtils.isEmpty(request.getMaxStaffAccount()))
                    || (staffAccountUnlimited && ObjectUtils.isNotEmpty(request.getMaxStaffAccount()))) {
                throw new InvalidPlanException(ErrorCode.INVALID_STAFF_ACCOUNT);
            }
        }
    }

    private PlanResponse buildResponse(Plan plan) {
        PlanResponse response = planMapper.toPlanResponse(plan);
        response.setFeatures(JsonUtil.convertJsonToFeatures(plan.getFeature()));
        return response;
    }
}
