package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApiException{

    public InvalidPasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
