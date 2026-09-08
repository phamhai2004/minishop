package com.example.minishop.security;

import com.example.minishop.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(
                properties.getSecret().getBytes()
        );
    }

    public String generateToken(Long userId) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + properties.getExpiration()
        );

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Long extractUserId(String token) {

        String subject = extractAllClaims(token).getSubject();

        return Long.valueOf(subject);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    public boolean isValidToken(
            String token,
            Long userId
    ) {

        return extractUserId(token).equals(userId)
                && !isTokenExpired(token);
    }

    public String generateRefreshToken(Long userId) {

        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(
                        new Date(
                                now.getTime()
                                        + 30L * 24 * 60 * 60 * 1000
                        )
                )
                .signWith(secretKey)
                .compact();
    }
}