package com.sr.Auth_Service.controller;

import com.sr.Auth_Service.Auth.AuthRequestDTO;
import com.sr.Auth_Service.Auth.RefreshRequestDTO;
import com.sr.Auth_Service.Auth.UserDTO;
import com.sr.Auth_Service.Model.Users;
import com.sr.Auth_Service.Repository.UserRepository;
import com.sr.Auth_Service.Service.JwtService;
import com.sr.Auth_Service.Service.RefreshTokenService;
import com.sr.Auth_Service.Service.TokenBlackListService;
import com.sr.Auth_Service.Service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenBlackListService tokenBlacklistService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthController(JwtService jwtService, TokenBlackListService tokenBlackListService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlackListService;
    }

    // Endpoint for new users to register
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody AuthRequestDTO requestDTO) {
        return ResponseEntity.ok(userService.register(requestDTO));
    }

    // Endpoint for existing users to log in and get an access token + refresh token
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody AuthRequestDTO requestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        // fetch user entity to get id/roles if you need ID in response
        Users userEntity = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Issue a long-lived opaque refresh token stored in Redis with TTL (e.g., 7 days)
        String refreshToken = refreshTokenService.mintRefreshToken(String.valueOf(userEntity.getId()), java.time.Duration.ofDays(7));

        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + token)
                .header("X-Refresh-Token", refreshToken)
                .header("X-User-Id", String.valueOf(userEntity.getId()))
                .header("X-Username", userEntity.getUsername())
                .header("X-User-Role", userEntity.getRoles().stream().findFirst().orElse("USER"))
                .build();
    }

    // Exchange a valid refresh token for a new access token
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@RequestBody(required = false) RefreshRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = request != null ? request.getUserId() : null;
        String providedRefreshToken = request != null ? request.getRefreshToken() : null;
        if (userId == null) {
            String headerUserId = httpRequest.getHeader("X-User-Id");
            if (headerUserId != null && !headerUserId.isBlank()) {
                try {
                    userId = Long.parseLong(headerUserId);
                } catch (NumberFormatException ignored) {}
            }
        }
        if (providedRefreshToken == null || providedRefreshToken.isBlank()) {
            providedRefreshToken = httpRequest.getHeader("X-Refresh-Token");
        }
        if (userId == null || providedRefreshToken == null || providedRefreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!refreshTokenService.validate(String.valueOf(user.getId()), providedRefreshToken)) {
            return ResponseEntity.status(401).build();
        }

        UserDetails userDetails = User
                .withUsername(user.getUsername())
                .password("")
                .authorities(user.getRoles().toArray(new String[0]))
                .build();

        String token = jwtService.generateToken(userDetails);

        // Rotate refresh token
        String newRefreshToken = refreshTokenService.mintRefreshToken(String.valueOf(user.getId()), java.time.Duration.ofDays(7));
        refreshTokenService.revoke(String.valueOf(user.getId()), providedRefreshToken);

        return ResponseEntity
                .ok()
                .header("Authorization", "Bearer " + token)
                .header("X-Refresh-Token", newRefreshToken)
                .header("X-User-Id", String.valueOf(user.getId()))
                .header("X-Username", user.getUsername())
                .header("X-User-Role", user.getRoles().stream().findFirst().orElse("USER"))
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            Claims claims = jwtService.extractAllClaims(jwt);
            String jti = claims.getId();
            tokenBlacklistService.blacklist(jti, claims.getExpiration());
        }
        // If client includes refresh token and user id in headers for logout, revoke it as well (optional pattern)
        String refreshToken = request.getHeader("X-Refresh-Token");
        String userId = request.getHeader("X-User-Id");
        if (refreshToken != null && userId != null) {
            refreshTokenService.revoke(userId, refreshToken);
        }
        return ResponseEntity.noContent().build();
    }

}