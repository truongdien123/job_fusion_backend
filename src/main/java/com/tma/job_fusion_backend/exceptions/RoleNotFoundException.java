package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends ApiException{

    public RoleNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
