package com.farmchainx.farmchainx.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Must be at least 32 chars for HS256
    private final String SECRET_KEY = "farmchainx_secret_key_1234567890!!farm"; 
    private final long EXPIRATION = 1000 * 60 * 60; // 1 hour validity

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 1. Generate JWT token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)                // put email inside token
                .setIssuedAt(new Date())          // issued time
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // expiry
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign with key
                .compact();
    }

    // 2. Extract email from token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())   // verify signature
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 3. Validate token
    public boolean validateToken(String token, String email) {
        String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isExpired(token));
    }

    // Check expiry
    private boolean isExpired(String token) {
        Date exp = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return exp.before(new Date());
    }
}
