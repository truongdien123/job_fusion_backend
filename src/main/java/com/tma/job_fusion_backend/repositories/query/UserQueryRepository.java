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

import com.tma.job_fusion_backend.pojo.dtos.StaffFilter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Page<User> findStaffByTenantId(UUID tenantId, String excludeRole, StaffFilter filter, Pageable pageable) {
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

        if (ObjectUtils.isNotEmpty(filter)) {
            if (StringUtils.isNotEmpty(filter.getSearch())) {
                String search = filter.getSearch().trim();
                predicate = predicate.and(
                        qUser.fullName.containsIgnoreCase(search)
                                .or(qUser.email.containsIgnoreCase(search))
                                .or(qUser.phone.containsIgnoreCase(search))
                                .or(qUser.employeeCode.containsIgnoreCase(search))
                                .or(qUser.jobTitle.containsIgnoreCase(search))
                );
            }
            if (StringUtils.isNotEmpty(filter.getFullName())) {
                predicate = predicate.and(qUser.fullName.containsIgnoreCase(filter.getFullName().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getEmail())) {
                predicate = predicate.and(qUser.email.containsIgnoreCase(filter.getEmail().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getPhone())) {
                predicate = predicate.and(qUser.phone.containsIgnoreCase(filter.getPhone().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getEmployeeCode())) {
                predicate = predicate.and(qUser.employeeCode.containsIgnoreCase(filter.getEmployeeCode().trim()));
            }
            if (StringUtils.isNotEmpty(filter.getJobTitle())) {
                predicate = predicate.and(qUser.jobTitle.containsIgnoreCase(filter.getJobTitle().trim()));
            }
            if (ObjectUtils.isNotEmpty(filter.getStatus())) {
                predicate = predicate.and(qUser.status.eq(filter.getStatus()));
            }
        }

        List<User> users = queryFactory.selectFrom(qUser)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qUser.createdAt.desc())
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
