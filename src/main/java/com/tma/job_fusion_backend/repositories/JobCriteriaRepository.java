package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.JobCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobCriteriaRepository extends JpaRepository<JobCriteria, UUID> {

    List<JobCriteria> findByJobIdAndDeletedAtIsNull(UUID jobId);

    Optional<JobCriteria> findByIdAndDeletedAtIsNull(UUID id);

}
