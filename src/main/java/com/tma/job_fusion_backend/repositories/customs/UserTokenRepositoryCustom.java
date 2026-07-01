package com.tma.job_fusion_backend.repositories.customs;

import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserTokenRepositoryCustom {
    Optional<UserToken> findByUserAndTokenTypeAndUsedAndExpiredAtAfter(
            User user, TokenType tokenType, boolean isUsed, LocalDateTime now);

    void invalidateOldToken(User user, TokenType tokenType, LocalDateTime now);
}
