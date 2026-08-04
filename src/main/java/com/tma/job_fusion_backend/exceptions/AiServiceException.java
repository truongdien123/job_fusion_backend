package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class AiServiceException extends ApiException {
    public AiServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY);
        initCause(cause);
    }
}
