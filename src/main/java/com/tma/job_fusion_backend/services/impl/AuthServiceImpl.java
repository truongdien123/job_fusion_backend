package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
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
import com.tma.job_fusion_backend.models.UserRole;
import com.tma.job_fusion_backend.models.RoleTenant;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.repositories.UserTokenRepository;
import com.tma.job_fusion_backend.repositories.UserRoleRepository;
import com.tma.job_fusion_backend.repositories.RoleTenantRepository;
import java.util.Optional;
import com.tma.job_fusion_backend.services.AuthService;
import com.tma.job_fusion_backend.services.EmailService;
import com.tma.job_fusion_backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final UserRoleRepository userRoleRepository;
    private final RoleTenantRepository roleTenantRepository;

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
        User user = checkUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ErrorCode.INVALID_PASSWORD);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(ErrorCode.INACTIVE_USER);
        }

        if (ObjectUtils.isNotEmpty(user.getDeletedAt())) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        String resolvedRole = resolveUserRole(user);

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
        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setUserRole(resolvedRole);
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = checkUserByEmail(request.getEmail());

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

    private String resolveUserRole(User user) {
        if (UserType.CANDIDATE == user.getType()) {
            return RoleConstant.CANDIDATE;
        }
        Optional<UserRole> userRoleOpt = userRoleRepository.findByUser(user);
        if (userRoleOpt.isPresent()) {
            UserRole userRole = userRoleOpt.get();
            if (ObjectUtils.isNotEmpty(userRole.getRolePlatform())) {
                return userRole.getRolePlatform().getName().equalsIgnoreCase("Super Admin")
                        ? RoleConstant.SUPER_ADMIN
                        : userRole.getRolePlatform().getName();
            } else if (ObjectUtils.isNotEmpty(userRole.getRoleTemplate())) {
                switch (userRole.getRoleTemplate().getName()) {
                    case "Tenant Admin":
                        return RoleConstant.TENANT_ADMIN;
                    case "HR":
                        return RoleConstant.HR;
                    case "Interviewer":
                        return RoleConstant.INTERVIEWER;
                }
            }
        }
        Optional<RoleTenant> roleTenantOpt = roleTenantRepository.findByUser(user);
        if (roleTenantOpt.isPresent()) {
            RoleTenant roleTenant = roleTenantOpt.get();
            if (StringUtils.isNotEmpty(roleTenant.getName())) {
                return roleTenant.getName();
            } else if (ObjectUtils.isNotEmpty(roleTenant.getRoleTemplate())) {
                return roleTenant.getRoleTemplate().getName();
            }
        }

        return null;
    }

    private User checkUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
