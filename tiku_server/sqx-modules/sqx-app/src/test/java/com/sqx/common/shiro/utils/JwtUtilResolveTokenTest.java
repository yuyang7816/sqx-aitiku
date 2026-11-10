package com.sqx.common.shiro.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * JwtUtil Token 解析单元测试
 */
class JwtUtilResolveTokenTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("f4e2e52034348f86b67cde581c0f9eb5");
        jwtUtil.setExpire(3600);
        jwtUtil.setHeader("token");
    }

    @Test
    void resolveToken_fromBearerHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + "abc.jwt.token");

        assertEquals("abc.jwt.token", jwtUtil.resolveToken(request));
    }

    @Test
    void resolveToken_fromLegacyHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "legacy-token");

        assertEquals("legacy-token", jwtUtil.resolveToken(request));
    }

    @Test
    void resolveToken_bearerHasPriorityOverLegacyHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + "bearer-token");
        request.addHeader("token", "legacy-token");

        assertEquals("bearer-token", jwtUtil.resolveToken(request));
    }

    @Test
    void resolveToken_fromQueryParamWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("token", "query-token");

        assertEquals("query-token", jwtUtil.resolveToken(request));
    }

    @Test
    void resolveToken_returnsNullWhenMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertNull(jwtUtil.resolveToken(request));
    }
}
