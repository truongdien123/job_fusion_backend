package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.models.Skill;
import com.tma.job_fusion_backend.repositories.SkillRepository;
import com.tma.job_fusion_backend.repositories.query.SkillQueryRepository;
import com.tma.job_fusion_backend.services.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillQueryRepository skillQueryRepository;

    @Override
    @Transactional
    public List<Skill> getOrCreateSkills(Collection<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> cleanedNames = skillNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .toList();

        if (cleanedNames.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> lowerCaseNames = cleanedNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<Skill> existingSkills = skillQueryRepository.findByNamesIgnoreCase(lowerCaseNames);
        Set<String> existingNamesLower = existingSkills.stream()
                .map(Skill::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<Skill> newSkills = cleanedNames.stream()
                .filter(name -> !existingNamesLower.contains(name.toLowerCase()))
                .map(name -> {
                    Skill skill = new Skill();
                    skill.setName(name);
                    return skill;
                })
                .collect(Collectors.toList());

        if (!newSkills.isEmpty()) {
            skillRepository.saveAll(newSkills);
            log.info("Saved {} new skills in bulk", newSkills.size());
            existingSkills.addAll(newSkills);
        }

        return existingSkills;
    }
}
