package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QJobPosting;
import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.pojo.dtos.JobPostingFilter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobPostingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Page<JobPosting> findAllJobPostings(UUID tenantId, JobPostingFilter filter, Pageable pageable) {
        QJobPosting qJobPosting = QJobPosting.jobPosting;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qJobPosting.deletedAt.isNull());

        if (ObjectUtils.isNotEmpty(tenantId)) {
            builder.and(qJobPosting.tenant.id.eq(tenantId));
        }

        if (ObjectUtils.isNotEmpty(filter)) {
            if (StringUtils.isNotEmpty(filter.getSearch())) {
                String search = filter.getSearch().trim();
                builder.and(qJobPosting.title.containsIgnoreCase(search)
                        .or(qJobPosting.department.containsIgnoreCase(search))
                        .or(qJobPosting.location.containsIgnoreCase(search))
                        .or(qJobPosting.description.containsIgnoreCase(search)));
            }
            if (StringUtils.isNotEmpty(filter.getTitle())) {
                builder.and(qJobPosting.title.containsIgnoreCase(filter.getTitle().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getDepartment())) {
                builder.and(qJobPosting.department.containsIgnoreCase(filter.getDepartment().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getLevel())) {
                builder.and(qJobPosting.level.containsIgnoreCase(filter.getLevel().trim()));
            }
            if (ObjectUtils.isNotEmpty(filter.getEmploymentType())) {
                builder.and(qJobPosting.employmentType.eq(filter.getEmploymentType()));
            }
            if (ObjectUtils.isNotEmpty(filter.getLocationType())) {
                builder.and(qJobPosting.locationType.eq(filter.getLocationType()));
            }
            if (ObjectUtils.isNotEmpty(filter.getStatus())) {
                builder.and(qJobPosting.status.eq(filter.getStatus()));
            }
        }

        List<JobPosting> content = queryFactory.selectFrom(qJobPosting)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qJobPosting.createdAt.desc())
                .fetch();

        Long total = queryFactory.select(qJobPosting.count())
                .from(qJobPosting)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
