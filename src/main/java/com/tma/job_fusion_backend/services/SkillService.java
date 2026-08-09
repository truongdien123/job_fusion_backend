package com.tma.job_fusion_backend.services;

import com.tma.job_fusion_backend.models.Skill;
import java.util.Collection;
import java.util.List;

public interface SkillService {
    List<Skill> getOrCreateSkills(Collection<String> skillNames);
}
