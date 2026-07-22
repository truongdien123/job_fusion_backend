package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QPlan;
import com.tma.job_fusion_backend.models.QTenant;
import com.tma.job_fusion_backend.models.Plan;
import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.PlanStatus;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.pojo.dtos.PlanFilter;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlanQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Page<Plan> findAllPlans(PlanFilter filter, Pageable pageable) {
        QPlan qPlan = QPlan.plan;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qPlan.deletedAt.isNull());

        if (ObjectUtils.isNotEmpty(filter)) {
            if (StringUtils.isNotEmpty(filter.getSearch())) {
                String search = filter.getSearch().trim();
                builder.and(qPlan.name.containsIgnoreCase(search)
                        .or(qPlan.description.containsIgnoreCase(search)));
            }
            if (StringUtils.isNotEmpty(filter.getName())) {
                builder.and(qPlan.name.containsIgnoreCase(filter.getName().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getDescription())) {
                builder.and(qPlan.description.containsIgnoreCase(filter.getDescription().trim()));
            }
            if (ObjectUtils.isNotEmpty(filter.getStatus())) {
                builder.and(qPlan.status.eq(filter.getStatus()));
            }
        }

        List<Plan> content = queryFactory.selectFrom(qPlan)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qPlan.createdAt.desc())
                .fetch();

        Long total = queryFactory.select(qPlan.count())
                .from(qPlan)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Transactional(readOnly = true)
    public Long countActivePlans() {
        QPlan qPlan = QPlan.plan;
        return queryFactory.select(qPlan.count())
                .from(qPlan)
                .where(qPlan.status.eq(PlanStatus.ACTIVE)
                        .and(qPlan.deletedAt.isNull()))
                .fetchOne();
    }

    @Transactional(readOnly = true)
    public Long countActivePlansCreatedThisMonth() {
        QPlan qPlan = QPlan.plan;
        LocalDateTime startOfMonth = DateTimeUtil.nowUtc()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return queryFactory.select(qPlan.count())
                .from(qPlan)
                .where(qPlan.status.eq(PlanStatus.ACTIVE)
                        .and(qPlan.deletedAt.isNull())
                        .and(qPlan.createdAt.goe(startOfMonth))) //goe = greater or equal
                .fetchOne();
    }

    @Transactional(readOnly = true)
    public Optional<Plan> findTopTierPlan() {
        QPlan qPlan = QPlan.plan;
        QTenant qTenant = QTenant.tenant;
        Plan plan = queryFactory.select(qPlan)
                .from(qPlan)
                .leftJoin(qTenant).on(qTenant.plan.id.eq(qPlan.id)
                        .and(qTenant.status.eq(TenantStatus.ACTIVE))
                        .and(qTenant.deletedAt.isNull()))
                .where(qPlan.status.eq(PlanStatus.ACTIVE)
                        .and(qPlan.deletedAt.isNull()))
                .groupBy(qPlan.id)
                .orderBy(qTenant.count().desc(), qPlan.monthlyPrice.desc())
                .limit(1)
                .fetchOne();
        return Optional.ofNullable(plan);
    }
}
