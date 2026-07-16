package com.tma.job_fusion_backend.utils;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.enums.TokenType;
import com.tma.job_fusion_backend.enums.UserType;
import com.tma.job_fusion_backend.models.Role;
import com.tma.job_fusion_backend.models.User;
import com.tma.job_fusion_backend.models.UserRole;
import com.tma.job_fusion_backend.models.UserToken;
import com.tma.job_fusion_backend.repositories.UserRoleRepository;
import com.tma.job_fusion_backend.repositories.UserTokenRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.access.AccessDeniedException;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

public final class UserUtil {

    private static final SecureRandom random = new SecureRandom();

    private UserUtil() {
        // Prevent instantiation
    }

    public static String generateEmployeeCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 2; i++) {
            sb.append((char) ('A' + random.nextInt(26)));
        }
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static void validateAccess(UUID targetUserId, User targetUser, UserPrincipal currentUser) {
        // 1. Super Admin can access any user
        if (currentUser.hasRole(RoleConstant.SUPER_ADMIN)) {
            return;
        }

        // 2. Candidate can only access their own detail
        if (currentUser.hasRole(RoleConstant.CANDIDATE)) {
            if (!currentUser.getId().equals(targetUserId)) {
                throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
            }
            return;
        }

        // 3. Tenant Admin / HR / Interviewer can access themselves OR users belonging to their same tenant
        if (!currentUser.getId().equals(targetUserId) && !isSameTenant(currentUser, targetUser)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }
    }

    public static void createAndSaveUserRole(User user, JwtUtil jwtUtil, UserTokenRepository userTokenRepository) {
        UserToken activationToken = new UserToken();
        activationToken.setUser(user);
        activationToken.setToken(jwtUtil.getActivationToken());
        activationToken.setTokenType(TokenType.ACTIVATION);
        activationToken.setExpiredAt(DateTimeUtil.nowUtc().plusDays(7));
        activationToken.setUsed(false);
        userTokenRepository.save(activationToken);
    }

    private static boolean isSameTenant(UserPrincipal currentUser, User targetUser) {
        if (ObjectUtils.isEmpty(currentUser.getTenantId()) || ObjectUtils.isEmpty(targetUser.getTenant())) {
            return false;
        }
        return currentUser.getTenantId().equals(targetUser.getTenant().getId());
    }

    public static String resolveUserRole(User user, UserRoleRepository userRoleRepository) {
        if (UserType.CANDIDATE == user.getType()) {
            return RoleConstant.CANDIDATE;
        }
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        if (ObjectUtils.isNotEmpty(userRoles)) {
            List<String> roleNames = userRoles.stream()
                    .map(UserRole::getRole)
                    .filter(ObjectUtils::isNotEmpty)
                    .map(Role::getName)
                    .filter(ObjectUtils::isNotEmpty)
                    .toList();
            if (!roleNames.isEmpty()) {
                return String.join(", ", roleNames);
            }
        }
        return null;
    }
}
