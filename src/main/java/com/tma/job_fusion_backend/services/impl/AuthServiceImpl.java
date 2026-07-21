package com.tma.job_fusion_backend.services.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.exceptions.*;
import com.tma.job_fusion_backend.pojo.requests.*;
import com.tma.job_fusion_backend.pojo.responses.ActivationDetailsResponse;
import com.tma.job_fusion_backend.pojo.responses.AuthResponse;
import com.tma.job_fusion_backend.pojo.responses.UserResponse;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.mappers.UserMapper;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserToken;
import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.repositories.UserTokenRepository;
import com.tma.job_fusion_backend.repositories.UserRoleRepository;

import com.tma.job_fusion_backend.repositories.query.UserTokenQueryRepository;
import com.tma.job_fusion_backend.services.AuthService;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.services.UserAuthCacheService;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import com.tma.job_fusion_backend.utils.JwtUtil;
import com.tma.job_fusion_backend.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

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
    private final UserRoleRepository userRoleRepository;
    private final UserTokenQueryRepository userTokenQueryRepository;
    private final UserAuthCacheService userAuthCacheService;

    @Override
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setType(UserType.CANDIDATE);
        user.setActivatedDate(DateTimeUtil.nowUtc());

        User savedUser = userRepository.save(user);
        savedUser.setCreatedBy(savedUser.getId());
        savedUser.setUpdatedBy(savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse signIn(SignInRequest request) {
        User user = checkUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ErrorCode.INVALID_PASSWORD);
        }

        return handleUserToResponse(user);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = checkUserByEmail(request.getEmail());

        userTokenQueryRepository.invalidateOldToken(user, TokenType.RESET_PASSWORD, DateTimeUtil.nowUtc());

        SecureRandom secureRandom = new SecureRandom();
        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(passwordEncoder.encode(otp));
        userToken.setTokenType(TokenType.RESET_PASSWORD);
        userToken.setExpiredAt(DateTimeUtil.nowUtc().plusMinutes(Integer.parseInt(expireMinutes)));
        userToken.setUsed(false);

        userTokenRepository.save(userToken);

        emailService.sendResetPasswordOtp(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = checkUserByEmail(request.getEmail());

        UserToken userToken = userTokenQueryRepository
                .findByUserAndTokenTypeAndUsedAndExpiredAtAfter(
                        user,
                        TokenType.RESET_PASSWORD,
                        false,
                        DateTimeUtil.nowUtc()
                )
                .orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_TOKEN));

        if (!passwordEncoder.matches(request.getOtp(), userToken.getToken())) {
            throw new BadRequestException(ErrorCode.INVALID_TOKEN);
        }

        userToken.setUsed(true);
        userTokenRepository.save(userToken);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRequirePasswordChange(false);
        user.setPasswordChangedAt(DateTimeUtil.nowUtc());
        userRepository.save(user);
        userAuthCacheService.updatePasswordChangedAt(user.getId(), user.getPasswordChangedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public void checkOTP(VerifyOtpRequest request) {
        User user = checkUserByEmail(request.getEmail());

        UserToken userToken = userTokenQueryRepository
                .findByUserAndTokenTypeAndUsedAndExpiredAtAfter(
                        user,
                        TokenType.RESET_PASSWORD,
                        false,
                        DateTimeUtil.nowUtc()
                )
                .orElseThrow(() -> new BadRequestException(ErrorCode.EXPIRED_OTP));
        if (!passwordEncoder.matches(request.getOtp(), userToken.getToken())) {
            throw new BadRequestException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        DecodedJWT decodedJWT;
        UUID userId;
        try {
            decodedJWT = jwtUtil.validateToken(request.getRefreshToken());
            userId = jwtUtil.getIdFromToken(decodedJWT);
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_TOKEN);
        }

        User user = checkUserById(userId);

        if (user.getPasswordChangedAt() != null) {
            Date issuedAt = jwtUtil.getIssuedAtFromToken(decodedJWT);
            long issuedAtMillis = issuedAt != null ? issuedAt.getTime() : 0;
            long passwordChangedAtMillis = DateTimeUtil.toEpochMilli(user.getPasswordChangedAt());

            if (issuedAtMillis < passwordChangedAtMillis) {
                throw new BadRequestException(ErrorCode.INVALID_TOKEN);
            }
        }

        return handleUserToResponse(user);
    }

    private AuthResponse handleUserToResponse(User user) {

        if (ObjectUtils.isNotEmpty(user.getDeletedAt())) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        String resolvedRole = UserUtil.resolveUserRole(user, userRoleRepository);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                ObjectUtils.isNotEmpty(user.getTenant()) ? user.getTenant().getId() : null,
                user.getFullName(),
                resolvedRole
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getId(),
                user.getEmail(),
                ObjectUtils.isNotEmpty(user.getTenant()) ? user.getTenant().getId() : null,
                user.getFullName(),
                resolvedRole
        );

        boolean mustChangePassword = Boolean.TRUE.equals(user.getRequirePasswordChange());

        // Pre-warm RAM Cache upon authentication/login so filter never hits DB
        userAuthCacheService.updatePasswordChangedAt(user.getId(), user.getPasswordChangedAt());

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setUserRole(resolvedRole);
        userResponse.setPlanId(ObjectUtils.isNotEmpty(user.getTenant()) ? user.getTenant().getPlan().getId() : null);
        userResponse.setRequirePasswordChange(mustChangePassword);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication) || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new InvalidCredentialsException(ErrorCode.INVALID_PASSWORD);
        }

        User user = checkUserById(principal.getId());

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException(ErrorCode.DUPLICATE_PASSWORD);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ErrorCode.INVALID_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setRequirePasswordChange(false);
        user.setPasswordChangedAt(DateTimeUtil.nowUtc());
        userRepository.save(user);
        userAuthCacheService.updatePasswordChangedAt(user.getId(), user.getPasswordChangedAt());

        SecurityContextHolder.clearContext();
    }

    private User checkUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private User checkUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public AuthResponse activateAccount(ActivationRequest request) {
        UserToken token = getUserToken(request.getToken(), DateTimeUtil.nowUtc());

        token.setUsed(true);
        userTokenRepository.save(token);

        User user = token.getUser();
        user.setStatus(UserStatus.ACTIVE);
        user.setActivatedDate(DateTimeUtil.nowUtc());
        User savedUser = userRepository.save(user);

        return handleUserToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivationDetailsResponse getActivationDetails(String tokenStr) {
        UserToken token = getUserToken(tokenStr, DateTimeUtil.nowUtc());

        User user = token.getUser();
        String resolvedRole = UserUtil.resolveUserRole(user, userRoleRepository);

        return ActivationDetailsResponse.builder()
                .workspaceName(user.getTenant() != null ? user.getTenant().getCompanyName() : null)
                .role(resolvedRole)
                .email(user.getEmail())
                .build();
    }

    private UserToken getUserToken(String token, LocalDateTime time) {
        return userTokenQueryRepository.findByTokenAndTokenTypeAndUsedAndExpiredAtAfter(
                token, TokenType.ACTIVATION, false, time
        ).orElseThrow(() -> new BadRequestException(ErrorCode.INVALID_TOKEN));
    }
}
