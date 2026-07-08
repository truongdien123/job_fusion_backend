package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class NotActiveException extends ApiException{

    public NotActiveException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
