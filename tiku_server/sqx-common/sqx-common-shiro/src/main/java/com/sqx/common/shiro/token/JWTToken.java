package com.sqx.common.shiro.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author chengming
 * @since 2025/4/30
 */
public class JWTToken implements AuthenticationToken {

    private final String token;

    public JWTToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
