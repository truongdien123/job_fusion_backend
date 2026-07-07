package com.tma.job_fusion_backend.repositories.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QTenant;
import com.tma.job_fusion_backend.models.QUserRole;
import com.tma.job_fusion_backend.pojo.responses.TenantResponse;
import com.tma.job_fusion_backend.repositories.customs.TenantRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Page<TenantResponse> findAllActiveTenants(Pageable pageable) {
        QTenant qTenant = QTenant.tenant;
        QUserRole qUserRole = QUserRole.userRole;

        JPQLQuery<UUID> adminUserIdSubquery = JPAExpressions.select(qUserRole.user.id)
                .from(qUserRole)
                .where(qUserRole.user.tenant.id.eq(qTenant.id)
                        .and(qUserRole.role.name.eq("Tenant Admin"))
                        .and(qUserRole.deletedAt.isNull())
                        .and(qUserRole.user.deletedAt.isNull()));

        List<TenantResponse> content = queryFactory.select(Projections.constructor(TenantResponse.class,
                        qTenant.id,
                        qTenant.companyName,
                        qTenant.domain,
                        qTenant.industry,
                        qTenant.companySize,
                        qTenant.region,
                        qTenant.status,
                        qTenant.plan.id,
                        adminUserIdSubquery
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
}
