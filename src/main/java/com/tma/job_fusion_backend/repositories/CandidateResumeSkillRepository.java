package com.tma.job_fusion_backend.repositories;
import com.tma.job_fusion_backend.models.CandidateResume;
import com.tma.job_fusion_backend.models.CandidateResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateResumeSkillRepository extends JpaRepository<CandidateResumeSkill, UUID> {
    void deleteByResume(CandidateResume resume);

    List<CandidateResumeSkill> findByResumeAndDeletedAtIsNull(CandidateResume resume);
}

