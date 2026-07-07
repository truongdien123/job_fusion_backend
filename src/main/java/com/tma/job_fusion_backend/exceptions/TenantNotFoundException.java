package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class TenantNotFoundException extends ApiException {
    public TenantNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
