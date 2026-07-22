package com.tma.job_fusion_backend.repositories.query;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.enums.TenantStatus;
import com.tma.job_fusion_backend.models.QTenant;
import com.tma.job_fusion_backend.models.QUser;
import com.tma.job_fusion_backend.models.QUserRole;
import com.tma.job_fusion_backend.models.Tenant;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.projections.TenantRevenueProjection;
import com.tma.job_fusion_backend.projections.TenantUsageProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
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
    public Page<TenantResponse> findAllActiveTenants(Pageable pageable) {
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
                        .and(qUser.deletedAt.isNull()));

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
                        qTenant.plan.maxStaffAccount,
                        adminUserIdSubquery,
                        qTenant.plan.monthlyPrice,
                        qTenant.expirationDate,
                        qTenant.createdAt
                ))
                .from(qTenant)
                .where(qTenant.deletedAt.isNull())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(qTenant.count())
                .from(qTenant)
                .where(qTenant.deletedAt.isNull())
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

        // get monthly price of each tenant using projection constructor
        List<TenantRevenueProjection> results = queryFactory.select(Projections.constructor(
                        TenantRevenueProjection.class,
                        qTenant.createdAt,
                        qTenant.deletedAt,
                        qTenant.plan.monthlyPrice
                ))
                .from(qTenant)
                .fetch();

        double totalRevenue = 0.0;
        LocalDateTime now = DateTimeUtil.nowUtc();

        for (TenantRevenueProjection projection : results) {
            LocalDateTime createdAt = projection.getCreatedAt();
            LocalDateTime deletedAt = projection.getDeletedAt();
            Double monthlyPrice = projection.getMonthlyPrice();

            if (createdAt != null && monthlyPrice != null) {
                LocalDateTime endDate = deletedAt != null ? deletedAt : now;

                // calculate number of months from when creating tenant to end date
                long months = Math.max(1, ChronoUnit.MONTHS.between(createdAt, endDate) + 1);
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

        // count user by tenant id
        JPQLQuery<Long> userCountSubquery = JPAExpressions.select(qUser.count())
                .from(qUser)
                .where(qUser.tenant.id.eq(qTenant.id)
                        .and(qUser.deletedAt.isNull()));

        // get active user and max staff of plan
        List<TenantUsageProjection> results = queryFactory.select(Projections.constructor(
                        TenantUsageProjection.class,
                        userCountSubquery,
                        qTenant.plan.maxStaffAccount
                ))
                .from(qTenant)
                .where(qTenant.status.eq(TenantStatus.ACTIVE)
                        .and(qTenant.deletedAt.isNull())
                        .and(qTenant.plan.maxStaffAccount.gt(0)))
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
