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
                        qTenant.price,
                        qTenant.billingCycle,
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

        // Fetch active tenants' createdAt, deletedAt, price, and billingCycle details
        List<TenantRevenueProjection> results = queryFactory.select(Projections.constructor(
                        TenantRevenueProjection.class,
                        qTenant.createdAt,
                        qTenant.deletedAt,
                        qTenant.price,
                        qTenant.billingCycle
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
                // Calculate used months: from createdAt to now (rounded up to at least 1 month)
                long months = Math.max(1, ChronoUnit.MONTHS.between(createdAt, now) + 1);
                
                // Convert plan price to monthly equivalent (divide by 12 for YEARLY plans, 6 for SIX_MONTHLY plans)
                double monthlyEquivalent = billingCycle == BillingCycle.YEARLY ? price / 12.0 
                        : (billingCycle == BillingCycle.SIX_MONTHLY ? price / 6.0 : price);
                
                // Accumulate tenant revenue: months * monthly equivalent price
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

        // Subquery to count the actual number of employees of the tenant (excluding Tenant Admin)
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

        // Get the active users count and max staff limit of active tenants
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
                // Efficiency of one Tenant = active users / max staff account limit
                totalUsage += (double) activeUsers / maxUsers;
                validCount++;
            }
        }

        // Return average usage efficiency across all active tenants (in %)
        return validCount > 0 ? (totalUsage / validCount) * 100.0 : 0.0;
    }

    @Transactional(readOnly = true)
    public Double calculateChurnRate() {
        QTenant qTenant = QTenant.tenant;

        // Determine the time boundary of the current quarter
        LocalDateTime now = DateTimeUtil.nowUtc();
        int currentMonth = now.getMonthValue();
        int startMonthOfQuarter = ((currentMonth - 1) / 3) * 3 + 1;
        LocalDateTime startOfQuarter = LocalDateTime.of(now.getYear(), startMonthOfQuarter, 1, 0, 0, 0);
        LocalDateTime endOfQuarter = startOfQuarter.plusMonths(3);

        // Numerator: Count tenants deleted or inactivated during the current quarter
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

        // Condition checking if tenant churned before the current quarter started
        BooleanExpression churnedBeforeStart = qTenant.deletedAt.isNotNull().and(qTenant.deletedAt.lt(startOfQuarter))
                .or(
                        qTenant.status.eq(TenantStatus.INACTIVE)
                                .and(qTenant.deletedAt.isNull())
                                .and(qTenant.updatedAt.isNotNull())
                                .and(qTenant.updatedAt.lt(startOfQuarter))
                );

        // Denominator: Count total tenants created before quarter end and not churned before start of quarter
        Long totalTenants = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(
                        qTenant.createdAt.lt(endOfQuarter)
                                .and(churnedBeforeStart.not())
                )
                .fetchOne();

        // Churn Rate (%) = (Churned Tenants in Quarter / Total Tenants present in Quarter) * 100%
        return ((double) churnedTenants / totalTenants) * 100.0;
    }

    @Transactional(readOnly = true)
    public Long countActiveSubscribersByPlanId(UUID planId) {
        QTenant qTenant = QTenant.tenant;
        return queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(qTenant.plan.id.eq(planId)
                        .and(qTenant.status.eq(TenantStatus.ACTIVE))
                        .and(qTenant.deletedAt.isNull()))
                .fetchOne();
    }

    @Transactional(readOnly = true)
    public Double calculateMonthlyActivePlanRevenue() {
        QTenant qTenant = QTenant.tenant;
        
        // Calculate Monthly Recurring Revenue (MRR) contribution: divide by 12 for YEARLY plans, 6 for SIX_MONTHLY, otherwise use price directly
        NumberExpression<Double> mrrContribution = new CaseBuilder()
                .when(qTenant.billingCycle.eq(BillingCycle.YEARLY))
                .then(qTenant.price.divide(12.0))
                .when(qTenant.billingCycle.eq(BillingCycle.SIX_MONTHLY))
                .then(qTenant.price.divide(6.0))
                .otherwise(qTenant.price);

        // Sum current Monthly Recurring Revenue (MRR) of active tenants
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
        // Get the first day of the current month at 00:00:00
        LocalDateTime startOfCurrentMonth = DateTimeUtil.nowUtc()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // Calculate MRR contribution for the previous month
        NumberExpression<Double> mrrContribution = new CaseBuilder()
                .when(qTenant.billingCycle.eq(BillingCycle.YEARLY))
                .then(qTenant.price.divide(12.0))
                .when(qTenant.billingCycle.eq(BillingCycle.SIX_MONTHLY))
                .then(qTenant.price.divide(6.0))
                .otherwise(qTenant.price);

        // Sum previous month's MRR of active tenants created before this month
        Double sum = queryFactory.select(mrrContribution.sum())
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.ACTIVE)
                        .and(qTenant.createdAt.lt(startOfCurrentMonth))
                        .and(qTenant.deletedAt.isNull().or(qTenant.deletedAt.goe(startOfCurrentMonth))))
                .fetchOne();
        return sum != null ? sum : 0.0;
    }
}
