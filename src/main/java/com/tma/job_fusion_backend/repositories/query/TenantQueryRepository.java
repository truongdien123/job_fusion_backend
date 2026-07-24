package com.tma.job_fusion_backend.repositories.query;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.ArrayList;

import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.tma.job_fusion_backend.enums.BillingCycle;
import org.springframework.data.domain.Sort;
import com.tma.job_fusion_backend.pojo.dtos.TenantFilter;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.enums.JobStatus;
import com.tma.job_fusion_backend.models.QJobPosting;
import com.tma.job_fusion_backend.models.QTenant;
import com.tma.job_fusion_backend.models.QUser;
import com.tma.job_fusion_backend.models.QUserRole;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.projections.TenantRevenueProjection;
import com.tma.job_fusion_backend.projections.TenantUsageProjection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TenantQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Page<TenantResponse> findAllActiveTenants(TenantFilter filter, Pageable pageable) {
        QTenant qTenant = QTenant.tenant;
        QUserRole qUserRole = QUserRole.userRole;
        QUser qUser = QUser.user;

        JPQLQuery<UUID> adminUserIdSubquery = JPAExpressions.select(qUserRole.user.id)
                .from(qUserRole)
                .where(qUserRole.user.tenant.id.eq(qTenant.id)
                        .and(qUserRole.role.name.eq(RoleConstant.TENANT_ADMIN))
                        .and(qUserRole.deletedAt.isNull())
                        .and(qUserRole.user.deletedAt.isNull()));

        JPQLQuery<Long> activeUsersSubquery = JPAExpressions.select(qUser.count())
                .from(qUser)
                .where(qUser.tenant.id.eq(qTenant.id)
                        .and(qUser.deletedAt.isNull())
                        .and(qUser.id.notIn(
                                JPAExpressions.select(qUserRole.user.id)
                                        .from(qUserRole)
                                        .where(qUserRole.role.name.eq(RoleConstant.TENANT_ADMIN)
                                                .and(qUserRole.deletedAt.isNull()))
                        )));

        QJobPosting qJobPosting = QJobPosting.jobPosting;
        JPQLQuery<Long> activeJobSubquery = JPAExpressions.select(qJobPosting.count())
                .from(qJobPosting)
                .where(qJobPosting.tenant.id.eq(qTenant.id)
                        .and(qJobPosting.status.eq(JobStatus.OPEN))
                        .and(qJobPosting.deletedAt.isNull()));

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTenant.deletedAt.isNull());

        if (ObjectUtils.isNotEmpty(filter)) {
            if (StringUtils.isNotEmpty(filter.getSearch())) {
                String search = filter.getSearch().trim();
                builder.and(qTenant.companyName.containsIgnoreCase(search));
            }
            if (ObjectUtils.isNotEmpty(filter.getStatus())) {
                builder.and(qTenant.status.eq(filter.getStatus()));
            }
            if (StringUtils.isNotEmpty(filter.getCompanyName())) {
                builder.and(qTenant.companyName.containsIgnoreCase(filter.getCompanyName().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getDomain())) {
                builder.and(qTenant.domain.containsIgnoreCase(filter.getDomain().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getIndustry())) {
                builder.and(qTenant.industry.containsIgnoreCase(filter.getIndustry().trim()));
            }
            if (ObjectUtils.isNotEmpty(filter.getPlanId())) {
                builder.and(qTenant.plan.id.eq(filter.getPlanId()));
            }
        }

        List<TenantResponse> content = queryFactory.select(Projections.constructor(TenantResponse.class,
                        qTenant.id,
                        qTenant.companyName,
                        qTenant.domain,
                        qTenant.industry,
                        qTenant.companySize,
                        qTenant.region,
                        qTenant.status,
                        qTenant.plan.id,
                        qTenant.plan.name,
                        activeUsersSubquery,
                        qTenant.maxStaffAccount,
                        adminUserIdSubquery,
                        activeJobSubquery,
                        qTenant.maxActiveJobPosting,
                        qTenant.plan.price,
                        qTenant.plan.billingCycle,
                        qTenant.expirationDate,
                        qTenant.createdAt
                ))
                .from(qTenant)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable, qTenant))
                .fetch();

        Long total = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(builder)
                .fetchOne();

        long totalCount = Optional.ofNullable(total).orElse(0L);

        return new PageImpl<>(content, pageable, totalCount);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable, QTenant qTenant) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                boolean isAsc = order.isAscending();
                String prop = order.getProperty();
                if ("companyName".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qTenant.companyName.asc() : qTenant.companyName.desc());
                } else if ("domain".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qTenant.domain.asc() : qTenant.domain.desc());
                } else if ("industry".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qTenant.industry.asc() : qTenant.industry.desc());
                } else if ("monthlyPrice".equalsIgnoreCase(prop) || "price".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qTenant.plan.price.asc() : qTenant.plan.price.desc());
                } else if ("createdAt".equalsIgnoreCase(prop)) {
                    specifiers.add(isAsc ? qTenant.createdAt.asc() : qTenant.createdAt.desc());
                }
            }
        }
        if (specifiers.isEmpty()) {
            specifiers.add(qTenant.createdAt.desc());
        }
        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }

    @Transactional(readOnly = true)
    public Optional<Tenant> findTenantWithDeletedAtIsNull(UUID id) {
        QTenant qTenant = QTenant.tenant;
        Tenant tenant = queryFactory.select(qTenant)
                .from(qTenant)
                .where(qTenant.id.eq(id).and(qTenant.deletedAt.isNull()))
                .fetchOne();
        return Optional.ofNullable(tenant);
    }

    @Transactional(readOnly = true)
    public Double calculateTotalRevenue() {
        QTenant qTenant = QTenant.tenant;

        // get price and billing cycle of active tenants using projection constructor
        List<TenantRevenueProjection> results = queryFactory.select(Projections.constructor(
                        TenantRevenueProjection.class,
                        qTenant.createdAt,
                        qTenant.deletedAt,
                        qTenant.plan.price,
                        qTenant.plan.billingCycle
                ))
                .from(qTenant)
                .where(qTenant.deletedAt.isNull()
                        .and(qTenant.status.eq(TenantStatus.ACTIVE)))
                .fetch();

        double totalRevenue = 0.0;
        LocalDateTime now = DateTimeUtil.nowUtc();

        for (TenantRevenueProjection projection : results) {
            LocalDateTime createdAt = projection.getCreatedAt();
            Double price = projection.getPrice();
            BillingCycle billingCycle = projection.getBillingCycle();

            if (createdAt != null && price != null) {
                // calculate number of months from when creating tenant to now
                long months = Math.max(1, ChronoUnit.MONTHS.between(createdAt, now) + 1);
                double monthlyEquivalent = billingCycle == BillingCycle.YEARLY ? price / 12.0 : price;
                totalRevenue += months * monthlyEquivalent;
            }
        }

        return totalRevenue;
    }

    @Transactional(readOnly = true)
    public Long countActiveTenants() {
        QTenant qTenant = QTenant.tenant;
        return queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.ACTIVE)
                        .and(qTenant.deletedAt.isNull()))
                .fetchOne();
    }

    @Transactional(readOnly = true)
    public Double calculateAverageUsage() {
        QTenant qTenant = QTenant.tenant;
        QUser qUser = QUser.user;
        QUserRole qUserRole = QUserRole.userRole;

        // count user by tenant id excluding tenant admin
        JPQLQuery<Long> userCountSubquery = JPAExpressions.select(qUser.count())
                .from(qUser)
                .where(qUser.tenant.id.eq(qTenant.id)
                        .and(qUser.deletedAt.isNull())
                        .and(qUser.id.notIn(
                                JPAExpressions.select(qUserRole.user.id)
                                        .from(qUserRole)
                                        .where(qUserRole.role.name.eq(RoleConstant.TENANT_ADMIN)
                                                .and(qUserRole.deletedAt.isNull()))
                        )));

        // get active user and max staff of tenant
        List<TenantUsageProjection> results = queryFactory.select(Projections.constructor(
                        TenantUsageProjection.class,
                        userCountSubquery,
                        qTenant.maxStaffAccount
                ))
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.ACTIVE)
                        .and(qTenant.deletedAt.isNull())
                        .and(qTenant.maxStaffAccount.gt(0)))
                .fetch();

        if (results.isEmpty()) {
            return 0.0;
        }

        double totalUsage = 0.0;
        int validCount = 0;
        for (TenantUsageProjection projection : results) {
            Long activeUsers = projection.getActiveUsers();
            Integer maxUsers = projection.getMaxUsers();
            if (activeUsers != null && maxUsers != null && maxUsers > 0) {
                totalUsage += (double) activeUsers / maxUsers;
                validCount++;
            }
        }

        return validCount > 0 ? (totalUsage / validCount) * 100.0 : 0.0;
    }

    @Transactional(readOnly = true)
    public Double calculateChurnRate() {
        QTenant qTenant = QTenant.tenant;

        LocalDateTime now = DateTimeUtil.nowUtc();
        int currentMonth = now.getMonthValue();
        int startMonthOfQuarter = ((currentMonth - 1) / 3) * 3 + 1;
        LocalDateTime startOfQuarter = LocalDateTime.of(now.getYear(), startMonthOfQuarter, 1, 0, 0, 0);
        LocalDateTime endOfQuarter = startOfQuarter.plusMonths(3);

        Long churnedTenants = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(
                        (qTenant.deletedAt.goe(startOfQuarter).and(qTenant.deletedAt.lt(endOfQuarter)))
                        .or(
                                qTenant.status.eq(TenantStatus.INACTIVE)
                                        .and(qTenant.deletedAt.isNull())
                                        .and(qTenant.updatedAt.goe(startOfQuarter).and(qTenant.updatedAt.lt(endOfQuarter)))
                        )
                )
                .fetchOne();

        BooleanExpression churnedBeforeStart = qTenant.deletedAt.lt(startOfQuarter)
                .or(
                        qTenant.status.eq(TenantStatus.INACTIVE)
                                .and(qTenant.deletedAt.isNull())
                                .and(qTenant.updatedAt.lt(startOfQuarter))
                );

        Long totalTenants = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(
                        qTenant.createdAt.lt(endOfQuarter)
                                .and(churnedBeforeStart.not())
                )
                .fetchOne();

        return ((double) churnedTenants / totalTenants) * 100.0;
    }

    @Transactional(readOnly = true)
    public Long countActiveSubscribersByPlanId(UUID planId) {
        QTenant qTenant = QTenant.tenant;
        Long count = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(qTenant.plan.id.eq(planId)
                        .and(qTenant.status.eq(TenantStatus.ACTIVE))
                        .and(qTenant.deletedAt.isNull()))
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Transactional(readOnly = true)
    public Double calculateMonthlyActivePlanRevenue() {
        QTenant qTenant = QTenant.tenant;
        NumberExpression<Double> mrrContribution = new CaseBuilder()
                .when(qTenant.plan.billingCycle.eq(BillingCycle.YEARLY))
                .then(qTenant.plan.price.divide(12.0))
                .otherwise(qTenant.plan.price);

        Double sum = queryFactory.select(mrrContribution.sum())
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.ACTIVE)
                        .and(qTenant.deletedAt.isNull()))
                .fetchOne();
        return sum != null ? sum : 0.0;
    }

    @Transactional(readOnly = true)
    public Double calculateMonthlyActivePlanRevenueLastMonth() {
        QTenant qTenant = QTenant.tenant;
        LocalDateTime startOfCurrentMonth = DateTimeUtil.nowUtc()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        NumberExpression<Double> mrrContribution = new CaseBuilder()
                .when(qTenant.plan.billingCycle.eq(BillingCycle.YEARLY))
                .then(qTenant.plan.price.divide(12.0))
                .otherwise(qTenant.plan.price);

        Double sum = queryFactory.select(mrrContribution.sum())
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.ACTIVE)
                        .and(qTenant.createdAt.lt(startOfCurrentMonth))
                        .and(qTenant.deletedAt.isNull().or(qTenant.deletedAt.goe(startOfCurrentMonth))))
                .fetchOne();
        return sum != null ? sum : 0.0;
    }
}
