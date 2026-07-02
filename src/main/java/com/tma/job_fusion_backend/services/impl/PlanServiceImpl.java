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
import com.tma.job_fusion_backend.components.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Override
    public PlanResponse createPlanResponse(CreatePlanRequest request) {
        if ((!request.getActiveJobPostingUnlimited() && ObjectUtils.isEmpty(request.getMaxActiveJobPosting())) || (request.getActiveJobPostingUnlimited() && request.getMaxActiveJobPosting() != null)) {
            throw new InvalidPlanException(ErrorCode.INVALID_JOB_POSTING);
        }
        if ((!request.getStaffAccountUnlimited() && ObjectUtils.isEmpty(request.getMaxStaffAccount())) || (request.getStaffAccountUnlimited() && request.getMaxStaffAccount() != null)) {
            throw new InvalidPlanException(ErrorCode.INVALID_STAFF_ACCOUNT);
        }
        Plan plan = planMapper.toEntity(request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            plan.setCreatedBy(principal.getId());
        }

        if (request.getFeatures() != null) {
            JsonNodeFactory factory = JsonNodeFactory.instance;
            ObjectNode rootNode = factory.objectNode();
            ArrayNode featuresArray = factory.arrayNode();
            for (FeatureDto featureDto : request.getFeatures()) {
                ObjectNode featureNode = factory.objectNode();
                featureNode.put("name", featureDto.getName());
                featureNode.put("description", featureDto.getDescription());
                featuresArray.add(featureNode);
            }
            rootNode.set("features", featuresArray);
            plan.setFeature(rootNode);
        }
        
        planRepository.save(plan);

        PlanResponse response = planMapper.toPlanResponse(plan);
        response.setFeatures(mapJsonNodeToFeatures(plan.getFeature()));

        return response;
    }

    @Override
    public Page<PlanResponse> getListPlan(Pageable pageable) {
        return planRepository.findAll(pageable).map(plan -> {
            PlanResponse response = planMapper.toPlanResponse(plan);
            response.setFeatures(mapJsonNodeToFeatures(plan.getFeature()));
            return response;
        });
    }

    private List<FeatureDto> mapJsonNodeToFeatures(JsonNode featureJson) {
        if (featureJson == null || !featureJson.has("features")) {
            return new ArrayList<>();
        }
        JsonNode featuresNode = featureJson.get("features");
        if (featuresNode == null || !featuresNode.isArray()) {
            return new ArrayList<>();
        }
        List<FeatureDto> featuresList = new ArrayList<>();
        for (JsonNode node : featuresNode) {
            FeatureDto feature = getFieldFromJson(node);
            featuresList.add(feature);
        }
        return featuresList;
    }

    private FeatureDto getFieldFromJson(JsonNode node) {
        String name = node.has("name") ? node.get("name").asText() : "";
        String description = node.has("description") ? node.get("description").asText() : "";
        return new FeatureDto(name, description);
    }
}
