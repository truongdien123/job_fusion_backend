package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.services.UserAuthCacheService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserAuthCacheServiceImpl implements UserAuthCacheService {

    private final UserRepository userRepository;

    // Thread-safe in-memory cache mapping userId -> passwordChangedAt epoch milliseconds.
    // A cached value of 0L indicates the user has no passwordChangedAt date set in DB.
    private final Map<UUID, Long> passwordChangedCache = new ConcurrentHashMap<>();

    @Override
    public Long getPasswordChangedAtMillis(UUID userId) {
        if (userId == null) {
            return null;
        }

        Long cachedMillis = passwordChangedCache.get(userId);
        if (cachedMillis != null) {
            return cachedMillis == 0L ? null : cachedMillis;
        }

        // Cache miss: load from DB once
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent() && userOptional.get().getPasswordChangedAt() != null) {
            long millis = DateTimeUtil.toEpochMilli(userOptional.get().getPasswordChangedAt());
            passwordChangedCache.put(userId, millis);
            return millis;
        } else {
            // Store 0L as sentinel value to avoid repeated DB lookups on cache misses
            passwordChangedCache.put(userId, 0L);
            return null;
        }
    }

    @Override
    public void updatePasswordChangedAt(UUID userId, LocalDateTime passwordChangedAt) {
        if (userId == null) {
            return;
        }
        if (passwordChangedAt != null) {
            long millis = DateTimeUtil.toEpochMilli(passwordChangedAt);
            passwordChangedCache.put(userId, millis);
        } else {
            passwordChangedCache.put(userId, 0L);
        }
    }

    @Override
    public void evictUser(UUID userId) {
        if (userId != null) {
            passwordChangedCache.remove(userId);
        }
    }
}
