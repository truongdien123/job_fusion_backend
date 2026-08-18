package com.tma.job_fusion_backend.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.mappers.AiGenerationHistoryMapper;
import com.tma.job_fusion_backend.models.AiGenerationHistory;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.dtos.AiHistoryFilter;
import com.tma.job_fusion_backend.pojo.responses.AiGenerationHistoryResponse;
import com.tma.job_fusion_backend.pojo.responses.PageResponse;
import com.tma.job_fusion_backend.repositories.AiGenerationHistoryRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.services.AiGenerationHistoryService;
import com.tma.job_fusion_backend.utils.JwtUtil;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AiGenerationHistoryServiceImpl implements AiGenerationHistoryService {

    private final AiGenerationHistoryRepository repository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final AiGenerationHistoryMapper mapper;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional
    public void saveHistory(String featureType, Object promptInput, Object generatedOutput) {
        try {
            UserPrincipal currentUser = jwtUtil.getCurrentUser();
            if (ObjectUtils.isEmpty(currentUser)) {
                log.warn("Cannot save AI history: no current user found");
                return;
            }

            UUID tenantId = currentUser.getTenantId();
            if (ObjectUtils.isEmpty(tenantId)) {
                log.warn("Cannot save AI history: current user has no tenant");
                return;
            }

            User user = userRepository.findByIdAndDeletedAtIsNull(currentUser.getId()).orElse(null);
            if (ObjectUtils.isEmpty(user)) {
                return;
            }

            JsonNode inputNode = objectMapper.valueToTree(promptInput);
            JsonNode outputNode = objectMapper.valueToTree(generatedOutput);

            AiGenerationHistory history = new AiGenerationHistory();
            history.setTenantId(tenantId);
            history.setUser(user);
            history.setFeatureType(featureType);
            history.setPromptInput(inputNode);
            history.setGeneratedOutput(outputNode);
            history.setCreatedBy(user.getId());

            repository.save(history);
            log.info("Saved AI generation history for feature: {} and tenant: {}", featureType, tenantId);
        } catch (Exception e) {
            log.error("Failed to save AI generation history: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiGenerationHistoryResponse> getHistoryList(PagingRequest<AiHistoryFilter> pagingRequest) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN) && !currentUser.hasRole(RoleConstant.HR)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        Pageable pageable = pagingRequest.toPageable();
        Page<AiGenerationHistory> page;

        String featureTypeFilter = (ObjectUtils.isNotEmpty(pagingRequest.getFilters()) 
                && StringUtils.isNotEmpty(pagingRequest.getFilters().getFeatureType()))
                ? pagingRequest.getFilters().getFeatureType()
                : null;

        if (StringUtils.isNotEmpty(featureTypeFilter)) {
            page = repository.findAllByTenantIdAndFeatureTypeAndDeletedAtIsNull(tenantId, featureTypeFilter, pageable);
        } else {
            page = repository.findAllByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        }

        return PageResponse.of(page.map(mapper::toResponse));
    }
}
