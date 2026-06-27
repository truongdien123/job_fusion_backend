package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.dtos.requests.SignInRequest;
import com.tma.job_fusion_backend.dtos.requests.SignUpRequest;
import com.tma.job_fusion_backend.dtos.responses.AuthResponse;
import com.tma.job_fusion_backend.dtos.responses.UserResponse;
import com.tma.job_fusion_backend.enums.UserStatus;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.exceptions.EmailAlreadyExistsException;
import com.tma.job_fusion_backend.exceptions.InvalidCredentialsException;
import com.tma.job_fusion_backend.exceptions.UserNotActiveException;
import com.tma.job_fusion_backend.mappers.UserMapper;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.services.AuthService;
import com.tma.job_fusion_backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        user.setType(UserType.CANDIDATE);

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException("User account is not active");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getType().name());

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toUserResponse(user))
                .build();
    }
}
