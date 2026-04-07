package com.sr.AUTH_SERVICE.Service;

import org.springframework.http.ResponseCookie;
import java.util.Set;

public interface JwtService {
    String generateToken(String username, Set<String> roles);

    ResponseCookie createRefreshCookie(String refreshToken);

    // You can add more here later, like validateToken or getUsernameFromToken
    String generateRefreshToken(String username);

    boolean validateToken(String token);

    String getUsernameFromToken(String token);

    void logout(String token);
}