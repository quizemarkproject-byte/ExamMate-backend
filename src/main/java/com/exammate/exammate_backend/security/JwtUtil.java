package com.exammate.exammate_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.exammate.exammate_backend.models.Role;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration-ms}")
    private long validityMillis;

    public String generateToken(String subject, UUID userId, Role role) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + validityMillis);

        return Jwts.builder()
                .claim("sub", subject)
                .claim("id", userId)
                .claim("role", role.name())
                .claim("iat", issuedAt)
                .claim("exp", expiration)
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractSubject(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null)
            return null;
        Object sub = claims.get("sub");
        return sub == null ? null : sub.toString();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null)
                return false;
            Object expObj = claims.get("exp");
            if (expObj instanceof Date) {
                return ((Date) expObj).after(new Date());
            }
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
