package com.tma.job_fusion_backend.annotations;

import com.tma.job_fusion_backend.utils.DateTimeUtil;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = DateTimeUtil.FutureOrPresentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureOrPresentDate {

    String message() default "Date must be in the future or present";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
