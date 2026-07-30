package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QActivityLog;
import com.tma.job_fusion_backend.models.ActivityLog;
import com.tma.job_fusion_backend.pojo.dtos.ActivityLogFilter;
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
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ActivityLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Page<ActivityLog> findAllActivityLogs(ActivityLogFilter filter, Pageable pageable) {
        QActivityLog qActivityLog = QActivityLog.activityLog;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qActivityLog.deletedAt.isNull());

        if (ObjectUtils.isNotEmpty(filter)) {
            if (ObjectUtils.isNotEmpty(filter.getEventType())) {
                builder.and(qActivityLog.eventType.eq(filter.getEventType()));
            }
            if (StringUtils.isNotEmpty(filter.getSearch())) {
                String search = filter.getSearch().trim();
                builder.and(qActivityLog.description.containsIgnoreCase(search)
                        .or(qActivityLog.ipAddress.containsIgnoreCase(search))
                        .or(qActivityLog.user.fullName.containsIgnoreCase(search)));
            }
            if (ObjectUtils.isNotEmpty(filter.getStartDate())) {
                builder.and(qActivityLog.createdAt.goe(filter.getStartDate()));
            }
            if (ObjectUtils.isNotEmpty(filter.getEndDate())) {
                builder.and(qActivityLog.createdAt.loe(filter.getEndDate()));
            }
            if (ObjectUtils.isNotEmpty(filter.getTenantId())) {
                builder.and(qActivityLog.user.tenant.id.eq(filter.getTenantId()));
            }
            if (ObjectUtils.isNotEmpty(filter.getUserId())) {
                builder.and(qActivityLog.user.id.eq(filter.getUserId()));
            }
        }

        List<ActivityLog> content = queryFactory.selectFrom(qActivityLog)
                .leftJoin(qActivityLog.user).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qActivityLog.updatedAt.desc())
                .fetch();

        Long total = queryFactory.select(qActivityLog.count())
                .from(qActivityLog)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Transactional
    public void softDeleteAllByUserId(UUID userId, LocalDateTime deletedAt, UUID updatedBy) {
        QActivityLog qActivityLog = QActivityLog.activityLog;

        queryFactory.update(qActivityLog)
                .set(qActivityLog.deletedAt, deletedAt)
                .set(qActivityLog.updatedBy, updatedBy)
                .where(qActivityLog.user.id.eq(userId)
                        .and(qActivityLog.deletedAt.isNull()))
                .execute();
    }
}
