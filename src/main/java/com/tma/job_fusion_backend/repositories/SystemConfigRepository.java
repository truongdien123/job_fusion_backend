package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {
    Optional<SystemConfig> findByConfigKeyAndDeletedAtIsNull(String configKey);
    List<SystemConfig> findByConfigGroupAndDeletedAtIsNull(String configGroup);
}
