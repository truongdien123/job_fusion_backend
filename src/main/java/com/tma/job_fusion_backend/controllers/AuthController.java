package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.dtos.requests.SignInRequest;
import com.tma.job_fusion_backend.dtos.requests.SignUpRequest;
import com.tma.job_fusion_backend.dtos.responses.AuthResponse;
import com.tma.job_fusion_backend.dtos.responses.UserResponse;
import com.tma.job_fusion_backend.services.AuthService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        UserResponse registeredUser = authService.signUp(request);
        return ResponseUtil.success("Sign up successful", registeredUser);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        AuthResponse response = authService.signIn(request);
        return ResponseUtil.success("Sign in successful", response);
    }
}
