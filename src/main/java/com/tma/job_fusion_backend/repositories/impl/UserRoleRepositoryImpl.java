package com.tma.job_fusion_backend.repositories.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QUserRole;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.repositories.customs.UserRoleRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserRoleRepositoryImpl implements UserRoleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findTenantAdminByTenantId(UUID tenantId) {
        QUserRole qUserRole = QUserRole.userRole;

        User result = queryFactory.select(qUserRole.user)
                .from(qUserRole)
                .where(qUserRole.user.tenant.id.eq(tenantId)
                        .and(qUserRole.role.name.eq("Tenant Admin")))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
