package com.sr.Auth_Service.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequestDTO {
    // The user identifier associated with the refresh token
    private Long userId;
    // The opaque refresh token string
    private String refreshToken;
}



