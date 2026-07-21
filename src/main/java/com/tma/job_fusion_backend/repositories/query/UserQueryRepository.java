package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.models.QUser;
import com.tma.job_fusion_backend.models.QUserRole;
import com.tma.job_fusion_backend.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Page<User> findStaffByTenantId(UUID tenantId, String excludeRole, Pageable pageable) {
        QUser qUser = QUser.user;
        QUserRole qUserRole = QUserRole.userRole;

        BooleanExpression predicate = qUser.tenant.id.eq(tenantId)
                .and(qUser.deletedAt.isNull())
                .and(qUser.id.notIn(
                        JPAExpressions.select(qUserRole.user.id)
                                .from(qUserRole)
                                .where(qUserRole.role.name.eq(excludeRole)
                                        .and(qUserRole.deletedAt.isNull()))
                ));

        List<User> users = queryFactory.selectFrom(qUser)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(qUser.count())
                .from(qUser)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(users, pageable, total);
    }

    @Transactional(readOnly = true)
    public long countStaffByTenantId(UUID tenantId, String excludeRole) {
        QUser qUser = QUser.user;
        QUserRole qUserRole = QUserRole.userRole;

        BooleanExpression predicate = qUser.tenant.id.eq(tenantId)
                .and(qUser.deletedAt.isNull())
                .and(qUser.id.notIn(
                        JPAExpressions.select(qUserRole.user.id)
                                .from(qUserRole)
                                .where(qUserRole.role.name.eq(excludeRole)
                                        .and(qUserRole.deletedAt.isNull()))
                ));

        return queryFactory.select(qUser.count())
                .from(qUser)
                .where(predicate)
                .fetchOne();
    }
}
