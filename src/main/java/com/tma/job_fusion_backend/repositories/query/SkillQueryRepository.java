package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QSkill;
import com.tma.job_fusion_backend.models.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SkillQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public List<Skill> findByNamesIgnoreCase(Collection<String> names) {
        QSkill qSkill = QSkill.skill;

        List<String> lowerCaseNames = names.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return queryFactory.selectFrom(qSkill)
                .where(qSkill.name.toLowerCase().in(lowerCaseNames))
                .fetch();
    }
}
