package com.tma.job_fusion_backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpiration;

    public String generateToken(String email, String userType) {
        return JWT.create()
                .withSubject(email)
                .withClaim("type", userType)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(String email, String userType) {
        return JWT.create()
                .withSubject(email)
                .withClaim("type", userType)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public String getEmailFromToken(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public String getUserTypeFromToken(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("type").asString();
    }
}
