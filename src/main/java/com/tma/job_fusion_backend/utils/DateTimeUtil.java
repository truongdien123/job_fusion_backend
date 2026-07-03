package com.tma.job_fusion_backend.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class DateTimeUtil {

    private DateTimeUtil() {
        // Prevent instantiation
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
