package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.pojo.requests.*;
import com.tma.job_fusion_backend.pojo.responses.AuthResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.exceptions.EmailAlreadyExistsException;
import com.tma.job_fusion_backend.exceptions.InvalidCredentialsException;
import com.tma.job_fusion_backend.exceptions.UserNotActiveException;
import com.tma.job_fusion_backend.exceptions.UserNotFoundException;
import com.tma.job_fusion_backend.exceptions.InvalidTokenException;
import com.tma.job_fusion_backend.mappers.UserMapper;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserToken;
import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.repositories.UserTokenRepository;
import com.tma.job_fusion_backend.services.AuthService;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${app.otp.expire-minutes}")
    private String expireMinutes;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserTokenRepository userTokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setType(UserType.CANDIDATE);
        user.setActivatedDate(LocalDateTime.now(ZoneOffset.UTC));

        User savedUser = userRepository.save(user);
        savedUser.setCreatedBy(savedUser.getId());
        savedUser.setUpdatedBy(savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(ErrorCode.INVALID_EMAIL));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ErrorCode.INVALID_PASSWORD);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(ErrorCode.INACTIVE_USER);
        }

        if (user.getDeletedAt() != null) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getTenant() != null ? user.getTenant().getId() : null,
                user.getFullName(),
                user.getType().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getTenant() != null ? user.getTenant().getId() : null,
                user.getFullName(),
                user.getType().name()
        );

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        userTokenRepository.invalidateOldToken(user, TokenType.RESET_PASSWORD, LocalDateTime.now(ZoneOffset.UTC));

        SecureRandom secureRandom = new SecureRandom();
        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(passwordEncoder.encode(otp));
        userToken.setTokenType(TokenType.RESET_PASSWORD);
        userToken.setExpiredAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(Integer.parseInt(expireMinutes)));
        userToken.setUsed(false);

        userTokenRepository.save(userToken);

        emailService.sendResetPasswordOtp(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = checkUserByEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void checkOTP(VerifyOtpRequest request) {
        User user = checkUserByEmail(request.getEmail());

        UserToken userToken = userTokenRepository
                .findByUserAndTokenTypeAndUsedAndExpiredAtAfter(
                        user,
                        TokenType.RESET_PASSWORD,
                        false,
                        LocalDateTime.now(ZoneOffset.UTC)
                )
                .orElseThrow(() -> new InvalidTokenException(ErrorCode.INVALID_TOKEN));
        if (!passwordEncoder.matches(request.getOtp(), userToken.getToken())) {
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN);
        }
        userToken.setUsed(true);
        userTokenRepository.save(userToken);
    }

    private User checkUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
