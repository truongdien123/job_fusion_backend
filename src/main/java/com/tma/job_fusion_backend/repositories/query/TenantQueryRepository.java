package com.tma.job_fusion_backend.repositories.query;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
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
                builder.and(qTenant.companyName.containsIgnoreCase(search)
                        .or(qTenant.domain.containsIgnoreCase(search))
                        .or(qTenant.industry.containsIgnoreCase(search))
                        .or(qTenant.plan.name.containsIgnoreCase(search)));
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
                        qTenant.plan.monthlyPrice,
                        qTenant.expirationDate,
                        qTenant.createdAt
                ))
                .from(qTenant)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qTenant.createdAt.desc())
                .fetch();

        Long total = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(builder)
                .fetchOne();

        long totalCount = Optional.ofNullable(total).orElse(0L);

        return new PageImpl<>(content, pageable, totalCount);
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

        // get monthly price of active tenants using projection constructor
        List<TenantRevenueProjection> results = queryFactory.select(Projections.constructor(
                        TenantRevenueProjection.class,
                        qTenant.createdAt,
                        qTenant.deletedAt,
                        qTenant.plan.monthlyPrice
                ))
                .from(qTenant)
                .where(qTenant.deletedAt.isNull()
                        .and(qTenant.status.eq(TenantStatus.ACTIVE)))
                .fetch();

        double totalRevenue = 0.0;
        LocalDateTime now = DateTimeUtil.nowUtc();

        for (TenantRevenueProjection projection : results) {
            LocalDateTime createdAt = projection.getCreatedAt();
            Double monthlyPrice = projection.getMonthlyPrice();

            if (createdAt != null && monthlyPrice != null) {
                // calculate number of months from when creating tenant to now
                long months = Math.max(1, ChronoUnit.MONTHS.between(createdAt, now) + 1);
                totalRevenue += months * monthlyPrice;
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

        Long totalTenants = queryFactory.select(qTenant.count())
                .from(qTenant)
                .fetchOne();

        Long churnedTenants = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.INACTIVE)
                        .or(qTenant.deletedAt.isNotNull()))
                .fetchOne();

        return ((double) churnedTenants / totalTenants) * 100.0;
    }
}
