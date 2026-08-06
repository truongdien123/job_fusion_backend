package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateResumeRepository extends JpaRepository<CandidateResume, UUID> {
    Optional<CandidateResume> findByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<CandidateResume> findFirstByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);
}
