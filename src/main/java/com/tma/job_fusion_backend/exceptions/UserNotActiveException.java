package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotActiveException extends ApiException {
    public UserNotActiveException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
