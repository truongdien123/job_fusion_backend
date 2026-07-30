package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import org.springframework.data.domain.Sort;
import com.tma.job_fusion_backend.models.QJobPosting;
import com.tma.job_fusion_backend.models.JobPosting;
import com.tma.job_fusion_backend.models.QCandidateApplication;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.pojo.dtos.JobPostingFilter;
import com.tma.job_fusion_backend.pojo.responses.DashboardStatsJobPostingResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
                builder.and(qJobPosting.title.containsIgnoreCase(search));
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
                .orderBy(getOrderSpecifiers(pageable, qJobPosting))
                .fetch();

        Long total = queryFactory.select(qJobPosting.count())
                .from(qJobPosting)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, QJobPosting qJobPosting) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                boolean isAsc = order.isAscending();
                String prop = order.getProperty();
                if ("title".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qJobPosting.title.asc() : qJobPosting.title.desc());
                } else if ("department".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qJobPosting.department.asc() : qJobPosting.department.desc());
                } else if ("location".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qJobPosting.location.asc() : qJobPosting.location.desc());
                } else if ("createdAt".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qJobPosting.createdAt.asc() : qJobPosting.createdAt.desc());
                }
            }
        }
        if (specifiers.isEmpty()) {
            specifiers.add(qJobPosting.createdAt.desc());
        }
        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }

    @Transactional(readOnly = true)
    public DashboardStatsJobPostingResponse getJobPostingStats(UUID tenantId, int soonDays) {
        QJobPosting qJobPosting = QJobPosting.jobPosting;
        QCandidateApplication qCandidateApplication = QCandidateApplication.candidateApplication;

        LocalDateTime now = DateTimeUtil.nowUtc();
        LocalDateTime soonLimit = now.plusDays(soonDays);

        Long totalActivePostings = queryFactory.select(qJobPosting.count())
                .from(qJobPosting)
                .where(qJobPosting.tenant.id.eq(tenantId)
                        .and(qJobPosting.status.eq(JobStatus.OPEN))
                        .and(qJobPosting.deletedAt.isNull()))
                .fetchOne();

        Long totalApplicants = queryFactory.select(qCandidateApplication.count())
                .from(qCandidateApplication)
                .where(qCandidateApplication.job.tenant.id.eq(tenantId)
                        .and(qCandidateApplication.deletedAt.isNull())
                        .and(qCandidateApplication.job.deletedAt.isNull()))
                .fetchOne();

        Long postingsExpiringSoon = queryFactory.select(qJobPosting.count())
                .from(qJobPosting)
                .where(qJobPosting.tenant.id.eq(tenantId)
                        .and(qJobPosting.status.eq(JobStatus.OPEN))
                        .and(qJobPosting.deletedAt.isNull())
                        .and(qJobPosting.applicationDeadline.between(now, soonLimit)))
                .fetchOne();

        return DashboardStatsJobPostingResponse.builder()
                .totalActivePostings(totalActivePostings)
                .totalApplicants(totalApplicants)
                .postingsExpiringSoon(postingsExpiringSoon)
                .build();
    }

    @Transactional
    public int closeExpiredJobPostings(LocalDateTime now) {
        QJobPosting qJobPosting = QJobPosting.jobPosting;
        return (int) queryFactory.update(qJobPosting)
                .set(qJobPosting.status, JobStatus.CLOSED)
                .set(qJobPosting.updatedAt, now)
                .where(qJobPosting.status.eq(JobStatus.OPEN)
                        .and(qJobPosting.applicationDeadline.before(now))
                        .and(qJobPosting.deletedAt.isNull()))
                .execute();
    }
}
