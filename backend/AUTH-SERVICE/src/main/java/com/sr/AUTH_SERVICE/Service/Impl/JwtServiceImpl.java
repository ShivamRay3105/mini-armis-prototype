package com.sr.AUTH_SERVICE.Service.Impl;

import com.sr.AUTH_SERVICE.Service.JwtService;
import com.sr.AUTH_SERVICE.Service.TokenBlacklistService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long expiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private final TokenBlacklistService blacklistService;

    private Key getSigningKey() {
        // JJWT requires a byte array for the key
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public String generateToken(String username, Set<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Prevents XSS
                .secure(false) // Set to true for Production (HTTPS)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict") // Prevents CSRF
                .build();
    }

    @Override
    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(token);
            return !blacklistService.isTokenBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void logout(String token) {
        // 1. Parse the token to get the Claims (data inside the JWT)
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // 2. Get the Expiration Date
        Date expiration = claims.getExpiration();

        // 3. Calculate remaining time (Expiration - Current Time)
        long remainingTime = expiration.getTime() - System.currentTimeMillis();

        // 4. If the token is already expired, no need to blacklist
        if (remainingTime > 0) {
            blacklistService.blacklistToken(token, remainingTime);
        }
    }
}