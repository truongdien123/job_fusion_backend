package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class PlanNotFoundException extends ApiException {
    public PlanNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
