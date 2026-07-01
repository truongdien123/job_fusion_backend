package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidPlanException extends ApiException{

    public InvalidPlanException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
