package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.models.QUserRole;
import com.tma.job_fusion_backend.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRoleQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Optional<User> findTenantAdminByTenantId(UUID tenantId) {
        QUserRole qUserRole = QUserRole.userRole;

        User result = queryFactory.select(qUserRole.user)
                .from(qUserRole)
                .where(qUserRole.user.tenant.id.eq(tenantId)
                        .and(qUserRole.role.name.eq(RoleConstant.TENANT_ADMIN)))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
