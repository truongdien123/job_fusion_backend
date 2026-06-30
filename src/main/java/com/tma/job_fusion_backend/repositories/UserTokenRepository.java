package com.tma.job_fusion_backend.repositories;

import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {
    Optional<UserToken> findByUserAndTokenTypeAndUsedAndExpiredAtAfter(
            User user, TokenType tokenType, boolean isUsed, LocalDateTime now);

    @Modifying
    @Query("update UserToken u set u.used = true where u.user = :user and u.tokenType = :token_type and u.used = false and u.expiredAt > :now")
    void invalidateOldToken(@Param("user") User user, @Param("token_type") TokenType tokenType, @Param("now") LocalDateTime now);
}
