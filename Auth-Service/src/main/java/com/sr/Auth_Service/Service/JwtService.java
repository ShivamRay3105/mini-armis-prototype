package com.sr.Auth_Service.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final Key signKey;
    private final TokenBlackListService tokenBlacklistService; // optional, autowired if present

    public JwtService(@Value("${jwt.secret}") String base64Secret,
            Optional<TokenBlackListService> tokenBlacklistService) {
        // decode the base64-encoded HMAC secret into raw bytes
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        // construct the signing key used for both signing and verification
        this.signKey = Keys.hmacShaKeyFor(keyBytes);
        // optionally wire in blacklist service so validation can reject logged-out tokens
        this.tokenBlacklistService = tokenBlacklistService.orElse(null);
    }

    public String generateToken(UserDetails userDetails) {
        // gather roles to embed as a claim for downstream authorization decisions
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // build a short-lived access token with unique jti for blacklist revocation
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setId(UUID.randomUUID().toString()) // jti
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)))
                .signWith(signKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        // parse and validate signature, then return the claims body
        return Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            if (tokenBlacklistService != null) {
                Claims claims = extractAllClaims(token);
                String jti = claims.getId();
                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    return false;
                }
            }
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // expose jti for logout
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

}
