package com.tma.job_fusion_backend.exceptions;

import org.springframework.http.HttpStatus;

public class FileUploadException extends ApiException {
    public FileUploadException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}
