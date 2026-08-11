package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.*;
import com.tma.job_fusion_backend.pojo.dtos.CandidateApplicationFilter;
import com.tma.job_fusion_backend.pojo.responses.CandidateApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CandidateApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Optional<CandidateApplication> findByCandidateIdAndJobIdWithResume(UUID candidateId, UUID jobId) {
        QCandidateApplication qApplication = QCandidateApplication.candidateApplication;
        QCandidateResume qResume = QCandidateResume.candidateResume;

        CandidateApplication application = queryFactory.selectFrom(qApplication)
                .leftJoin(qApplication.resume, qResume).fetchJoin()
                .where(qApplication.candidate.id.eq(candidateId)
                        .and(qApplication.job.id.eq(jobId))
                        .and(qApplication.deletedAt.isNull())
                        .and(qResume.deletedAt.isNull().or(qApplication.resume.isNull())))
                .fetchOne();

        return Optional.ofNullable(application);
    }

    @Transactional(readOnly = true)
    public Page<CandidateApplicationResponse> findApplicationsByTenant(UUID tenantId, CandidateApplicationFilter filter, Pageable pageable) {
        QCandidateApplication qApplication = QCandidateApplication.candidateApplication;
        QJobPosting qJobPosting = QJobPosting.jobPosting;
        QUser qCandidate = QUser.user;
        QCvMatchingResult qCvMatchingResult = QCvMatchingResult.cvMatchingResult;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qApplication.deletedAt.isNull());
        builder.and(qJobPosting.tenant.id.eq(tenantId));

        if (ObjectUtils.isNotEmpty(filter)) {
            if (StringUtils.isNotEmpty(filter.getSearch())) {
                String search = filter.getSearch().trim();
                builder.and(qCandidate.fullName.containsIgnoreCase(search)
                        .or(qJobPosting.title.containsIgnoreCase(search)));
            }
            if (ObjectUtils.isNotEmpty(filter.getJobId())) {
                builder.and(qJobPosting.id.eq(filter.getJobId()));
            }
            if (ObjectUtils.isNotEmpty(filter.getStatus())) {
                builder.and(qApplication.status.eq(filter.getStatus()));
            }
            if (ObjectUtils.isNotEmpty(filter.getMatchScoreLevel())) {
                switch (filter.getMatchScoreLevel()) {
                    case LOW -> builder.and(qCvMatchingResult.matchingScore.goe(0.0).and(qCvMatchingResult.matchingScore.lt(50.0)));
                    case MEDIUM -> builder.and(qCvMatchingResult.matchingScore.goe(50.0).and(qCvMatchingResult.matchingScore.lt(90.0)));
                    case HIGH -> builder.and(qCvMatchingResult.matchingScore.goe(90.0).and(qCvMatchingResult.matchingScore.loe(100.0)));
                }
            }
            if (ObjectUtils.isNotEmpty(filter.getAppliedDateFrom())) {
                builder.and(qApplication.appliedAt.goe(filter.getAppliedDateFrom()));
            }
            if (ObjectUtils.isNotEmpty(filter.getAppliedDateTo())) {
                builder.and(qApplication.appliedAt.loe(filter.getAppliedDateTo()));
            }
        }

        List<CandidateApplicationResponse> content = queryFactory.select(Projections.constructor(CandidateApplicationResponse.class,
                qApplication.id,
                qCandidate.id,
                qCandidate.fullName,
                qCandidate.email,
                qJobPosting.id,
                qJobPosting.title,
                qJobPosting.department,
                qCvMatchingResult.matchingScore,
                qApplication.status,
                qApplication.appliedAt,
                qApplication.reviewed
        ))
                .from(qApplication)
                .join(qApplication.job, qJobPosting)
                .join(qApplication.candidate, qCandidate)
                .leftJoin(qCvMatchingResult).on(qCvMatchingResult.application.id.eq(qApplication.id)
                        .and(qCvMatchingResult.deletedAt.isNull()))
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable, qApplication, qCandidate, qJobPosting, qCvMatchingResult))
                .fetch();

        Long total = queryFactory.select(qApplication.count())
                .from(qApplication)
                .join(qApplication.job, qJobPosting)
                .join(qApplication.candidate, qCandidate)
                .leftJoin(qCvMatchingResult).on(qCvMatchingResult.application.id.eq(qApplication.id)
                        .and(qCvMatchingResult.deletedAt.isNull()))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, QCandidateApplication qApplication, QUser qCandidate, QJobPosting qJobPosting, QCvMatchingResult qCvMatchingResult) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                boolean isAsc = order.isAscending();
                String prop = order.getProperty();
                if ("candidateName".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qCandidate.fullName.asc() : qCandidate.fullName.desc());
                } else if ("jobTitle".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qJobPosting.title.asc() : qJobPosting.title.desc());
                } else if ("matchScore".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qCvMatchingResult.matchingScore.asc() : qCvMatchingResult.matchingScore.desc());
                } else if ("status".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qApplication.status.asc() : qApplication.status.desc());
                } else if ("appliedAt".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qApplication.appliedAt.asc() : qApplication.appliedAt.desc());
                } else if ("createdAt".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qApplication.createdAt.asc() : qApplication.createdAt.desc());
                }
            }
        }
        if (specifiers.isEmpty()) {
            specifiers.add(qApplication.createdAt.desc());
        }
        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }

    @Transactional(readOnly = true)
    public long countByJobId(UUID jobId) {
        QCandidateApplication qApplication = QCandidateApplication.candidateApplication;
        return queryFactory.select(qApplication.count())
                .from(qApplication)
                .where(qApplication.job.id.eq(jobId)
                        .and(qApplication.deletedAt.isNull()))
                .fetchOne();
    }

    @Transactional(readOnly = true)
    public Map<UUID, Long> countByJobIds(List<UUID> jobIds) {
        QCandidateApplication qApplication = QCandidateApplication.candidateApplication;
        List<Tuple> results = queryFactory.select(qApplication.job.id, qApplication.count())
                .from(qApplication)
                .where(qApplication.job.id.in(jobIds)
                        .and(qApplication.deletedAt.isNull()))
                .groupBy(qApplication.job.id)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(qApplication.job.id),
                        tuple -> tuple.get(qApplication.count())
                ));
    }
}
