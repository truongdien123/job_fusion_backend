package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.AiGenerationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AiGenerationHistoryRepository extends JpaRepository<AiGenerationHistory, UUID> {
    Page<AiGenerationHistory> findAllByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);
    Page<AiGenerationHistory> findAllByTenantIdAndFeatureTypeAndDeletedAtIsNull(UUID tenantId, String featureType, Pageable pageable);
}
