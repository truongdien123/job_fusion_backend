package com.tma.job_fusion_backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_resume_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResumeSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private CandidateResume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "proficiency_level")
    private String proficiencyLevel;

}
