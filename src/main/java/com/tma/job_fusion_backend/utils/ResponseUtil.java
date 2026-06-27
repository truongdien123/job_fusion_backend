package com.tma.job_fusion_backend.utils;

import com.tma.job_fusion_backend.base.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static ResponseEntity<?> success(String message, Object data) {
        return ResponseEntity.ok(new ApiResponse<>(message, data));
    }

    public static ResponseEntity<?> error(HttpStatus status, String message) {
        return new ResponseEntity<>(new ApiResponse<>(message), status);
    }
}
