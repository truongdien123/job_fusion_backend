package com.tma.job_fusion_backend.controllers;

import com.tma.job_fusion_backend.commons.EndpointConstant;
import com.tma.job_fusion_backend.pojo.requests.*;
import com.tma.job_fusion_backend.pojo.responses.ActivationDetailsResponse;
import com.tma.job_fusion_backend.pojo.responses.AuthResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.services.AuthService;
import com.tma.job_fusion_backend.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointConstant.ENDPOINT_AUTH)
@RequiredArgsConstructor
@Tag(name = "authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping(EndpointConstant.ENDPOINT_SIGNUP_BASE)
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        UserResponse registeredUser = authService.signUp(request);
        return ResponseUtil.success("Sign up successful", registeredUser);
    }

    @PostMapping(EndpointConstant.ENDPOINT_ACTIVATE_BASE)
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivationRequest request) {
        AuthResponse response = authService.activateAccount(request);
        return ResponseUtil.success("Account activated successfully", response);
    }

    @GetMapping(EndpointConstant.ENDPOINT_ACTIVATE_BASE)
    public ResponseEntity<?> getActivationDetails(@RequestParam String token) {
        ActivationDetailsResponse response = authService.getActivationDetails(token);
        return ResponseUtil.success("Get activation details successfully", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_SIGNIN_BASE)
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        AuthResponse response = authService.signIn(request);
        return ResponseUtil.success("Sign in successful", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_FORGOT_PASSWORD_BASE)
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseUtil.success("OTP sent to your email successfully", null);
    }

    @PostMapping(EndpointConstant.ENDPOINT_RESET_PASSWORD_BASE)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseUtil.success("Password reset successfully", null);
    }

    @PostMapping(EndpointConstant.ENDPOINT_CHECK_OTP_BASE)
    public ResponseEntity<?> checkOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.checkOTP(request);
        return ResponseUtil.success("Verify otp successfully", null);
    }

    @PostMapping(EndpointConstant.ENDPOINT_REFRESH_BASE)
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseUtil.success("Refresh token successful", response);
    }

    @PostMapping(EndpointConstant.ENDPOINT_LOGOUT_BASE)
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseUtil.success("Logout successful", null);
    }

    @PostMapping(EndpointConstant.ENDPOINT_CHANGE_PASSWORD_BASE)
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseUtil.success("Password changed successfully", null);
    }
}
