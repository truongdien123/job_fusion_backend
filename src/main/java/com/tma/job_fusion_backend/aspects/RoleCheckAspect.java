package com.tma.job_fusion_backend.aspects;

import com.tma.job_fusion_backend.annotations.RequireRoles;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final JwtUtil jwtUtil;

    @Before("@annotation(com.tma.job_fusion_backend.annotations.RequireRoles) || @within(com.tma.job_fusion_backend.annotations.RequireRoles)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequireRoles requireRoles = method.getAnnotation(RequireRoles.class);
        if (requireRoles == null) {
            try {
                Method targetMethod = joinPoint.getTarget().getClass()
                        .getMethod(method.getName(), method.getParameterTypes());
                requireRoles = targetMethod.getAnnotation(RequireRoles.class);
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (requireRoles == null) {
            requireRoles = joinPoint.getTarget().getClass().getAnnotation(RequireRoles.class);
        }

        if (requireRoles == null) {
            return;
        }

        UserPrincipal principal = jwtUtil.getCurrentUser();
        if (principal == null) {
            throw new AccessDeniedException("Access denied");
        }

        boolean hasRole = Arrays.stream(requireRoles.value())
                .anyMatch(roleName -> principal.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_" + roleName)));

        if (!hasRole) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
