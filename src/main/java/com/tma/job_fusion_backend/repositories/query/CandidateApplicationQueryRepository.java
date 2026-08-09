package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.CandidateApplication;
import com.tma.job_fusion_backend.models.QCandidateApplication;
import com.tma.job_fusion_backend.models.QCandidateResume;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
}
