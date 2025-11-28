package com.tran.pulse.auth.util;

import com.tran.pulse.auth.service.JwtService;
import lombok.Data;

/**
 * Token对象，包含Access Token和Refresh Token
 */
@Data
public class TokenPair {
    private final String accessToken;
    private final String refreshToken;

    public TokenPair(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}