package com.tma.job_fusion_backend.utils;

import com.tma.job_fusion_backend.annotations.FutureOrPresentDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class DateTimeUtil {

    private DateTimeUtil() {
        // Prevent instantiation
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static long toEpochMilli(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0L;
        }
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static class FutureOrPresentValidator implements ConstraintValidator<FutureOrPresentDate, LocalDateTime> {
        @Override
        public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
            if (ObjectUtils.isEmpty(value)) {
                return true;
            }
            return !value.isBefore(nowUtc());
        }
    }
}
