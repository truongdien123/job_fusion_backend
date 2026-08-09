package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.models.CandidateApplication;
import com.tma.job_fusion_backend.models.CvMatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CvMatchingResultRepository extends JpaRepository<CvMatchingResult, UUID> {
    Optional<CvMatchingResult> findByApplication(CandidateApplication application);

    Optional<CvMatchingResult> findByApplicationAndDeletedAtIsNull(CandidateApplication application);
}

