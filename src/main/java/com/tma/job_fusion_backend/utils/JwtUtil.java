package com.tma.job_fusion_backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpiration;

    private Algorithm algorithm;

    private JWTVerifier jwtVerifier;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secret);
        this.jwtVerifier = JWT.require(algorithm).build();
    }

    public String generateToken(UUID id, String email, UUID tenantId, String fullName, String userType) {
        var builder = JWT.create()
                .withSubject(email)
                .withClaim("id", id.toString())
                .withClaim("fullName", fullName)
                .withClaim("type", userType);
        if (tenantId != null) {
            builder.withClaim("tenantId", tenantId.toString());
        }
        return builder.withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(UUID id, String email, UUID tenantId, String fullName, String userType) {
        var builder = JWT.create()
                .withSubject(email)
                .withClaim("id", id.toString())
                .withClaim("fullName", fullName)
                .withClaim("type", userType);
        if (tenantId != null) {
            builder.withClaim("tenantId", tenantId.toString());
        }
        return builder.withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateToken(String token) {
        return jwtVerifier.verify(token);
    }

    public String getEmailFromToken(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public String getUserTypeFromToken(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("type").asString();
    }

    public UUID getIdFromToken(DecodedJWT decodedJWT) {
        String id = decodedJWT.getClaim("id").asString();
        return id != null ? UUID.fromString(id) : null;
    }

    public UUID getTenantIdFromToken(DecodedJWT decodedJWT) {
        String tenantId = decodedJWT.getClaim("tenantId").asString();
        return tenantId != null ? UUID.fromString(tenantId) : null;
    }

    public String getFullNameFromToken(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("fullName").asString();
    }
}

