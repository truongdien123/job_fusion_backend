package com.tma.job_fusion_backend.components;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.tma.job_fusion_backend.services.UserAuthCacheService;
import com.tma.job_fusion_backend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserAuthCacheService userAuthCacheService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT decodedJWT = jwtUtil.validateToken(token);
                String email = jwtUtil.getEmailFromToken(decodedJWT);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UUID id = jwtUtil.getIdFromToken(decodedJWT);

                    // RAM Cache Check: Instant invalidation if token was issued before password change
                    Long passwordChangedAtMillis = userAuthCacheService.getPasswordChangedAtMillis(id);
                    if (passwordChangedAtMillis != null) {
                        Date issuedAt = jwtUtil.getIssuedAtFromToken(decodedJWT);
                        long issuedAtMillis = issuedAt != null ? issuedAt.getTime() : 0;
                        if (issuedAtMillis < passwordChangedAtMillis) {
                            SecurityContextHolder.clearContext();
                            filterChain.doFilter(request, response);
                            return;
                        }
                    }

                    UUID tenantId = jwtUtil.getTenantIdFromToken(decodedJWT);
                    String fullName = jwtUtil.getFullNameFromToken(decodedJWT);
                    String type = jwtUtil.getUserTypeFromToken(decodedJWT);

                    UserPrincipal principal = new UserPrincipal(id, email, tenantId, fullName, type);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

