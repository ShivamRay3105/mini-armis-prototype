package com.sr.AUTH_SERVICE.Controller;

import com.sr.AUTH_SERVICE.DB.User;
import com.sr.AUTH_SERVICE.Repository.UserRepository;
import com.sr.AUTH_SERVICE.Service.JwtService;
import com.sr.AUTH_SERVICE.dto.AuthRequest;
import com.sr.AUTH_SERVICE.dto.AuthResponse;
import com.sr.AUTH_SERVICE.utils.UserMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AuthRequest request) {
        User user = User.builder()
                .username(request.username()) // Record style accessor
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of("ROLE_USER"))
                .build();
        if (user != null)
            userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getUsername(), user.getRoles());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername()); // REAL TOKEN

        ResponseCookie cookie = jwtService.createRefreshCookie(refreshToken);
        AuthResponse response = userMapper.toAuthResponse(user, accessToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken") String refreshToken) {

        if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtService.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Issue a fresh Access Token
        String newAccessToken = jwtService.generateToken(user.getUsername(), user.getRoles());

        // Wrap in your Record DTO
        AuthResponse response = userMapper.toAuthResponse(user, newAccessToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        // Bearer <token> -> remove "Bearer " prefix
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.logout(token);

            // Also clear the Refresh Token cookie by setting its maxAge to 0
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .path("/")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body("Logged out successfully and token blacklisted.");
        }
        return ResponseEntity.badRequest().body("Invalid Authorization header");
    }
}