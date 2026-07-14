package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Optional<Plan> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByNameAndDeletedAtIsNull(String name);
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, UUID id);
}
