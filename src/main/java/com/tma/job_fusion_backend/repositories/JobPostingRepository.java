package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.models.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {

    Optional<JobPosting> findByIdAndDeletedAtIsNull(UUID id);

    long countByTenantIdAndStatusAndDeletedAtIsNull(UUID tenantId, JobStatus status);

    boolean existsByTenantIdAndTitleIgnoreCaseAndDeletedAtIsNull(UUID tenantId, String title);

    boolean existsByTenantIdAndTitleIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID tenantId, String title, UUID id);
}
