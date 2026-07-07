package com.tma.job_fusion_backend.repositories.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.models.QUserToken;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserTokenQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public Optional<UserToken> findByUserAndTokenTypeAndUsedAndExpiredAtAfter(
            User user, TokenType tokenType, boolean isUsed, LocalDateTime now) {
        QUserToken qUserToken = QUserToken.userToken;

        UserToken result = queryFactory.selectFrom(qUserToken)
                .where(qUserToken.user.eq(user)
                        .and(qUserToken.tokenType.eq(tokenType))
                        .and(qUserToken.used.eq(isUsed))
                        .and(qUserToken.expiredAt.gt(now)))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Transactional
    public void invalidateOldToken(User user, TokenType tokenType, LocalDateTime now) {
        QUserToken qUserToken = QUserToken.userToken;

        queryFactory.update(qUserToken)
                .set(qUserToken.used, true)
                .where(qUserToken.user.eq(user)
                        .and(qUserToken.tokenType.eq(tokenType))
                        .and(qUserToken.used.eq(false))
                        .and(qUserToken.expiredAt.gt(now)))
                .execute();
    }
}
