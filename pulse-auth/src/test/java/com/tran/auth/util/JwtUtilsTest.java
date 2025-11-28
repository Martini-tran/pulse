package com.tran.auth.util;

import com.tran.pulse.auth.properties.AuthProperties;
import com.tran.pulse.auth.util.JwtUtils;
import com.tran.pulse.common.util.StringIdGenerator;
import org.junit.Test;

public class JwtUtilsTest {



    @Test
    public void generateToken() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setSecret("VeryStrongSecretAtLeast32Characters!"); // Longer secret
        authProperties.setExpireSeconds(1800); // 30 minutes

        JwtUtils.setAuthProperties(authProperties);

        String id = StringIdGenerator.next();
        String token = JwtUtils.generateToken(id);

        System.out.println("Generated token: " + token);
        System.out.println("Is token valid: " + JwtUtils.validateToken(token));
        System.out.println("Username from token: " + JwtUtils.getUsernameFromToken(token));
        System.out.println("Is token expired: " + JwtUtils.isTokenExpired(token));
    }
}