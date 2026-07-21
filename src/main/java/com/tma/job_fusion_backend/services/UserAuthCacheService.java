package com.tma.job_fusion_backend.services;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserAuthCacheService {
    Long getPasswordChangedAtMillis(UUID userId);
    void updatePasswordChangedAt(UUID userId, LocalDateTime passwordChangedAt);
    void evictUser(UUID userId);
}
