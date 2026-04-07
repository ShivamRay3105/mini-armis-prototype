package com.sr.AUTH_SERVICE.dto;

import java.util.Set;

public record AuthResponse(
        String username,
        Set<String> roles,
        String accessToken) {
}