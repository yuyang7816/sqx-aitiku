package com.sqx.app.base.auth.auth.interceptor;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.constant.AppAuthConstants;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.shiro.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * APP 端 JWT 鉴权拦截器单元测试
 */
class AuthorizationInterceptorTest {

    private static final String TEST_SECRET = "f4e2e52034348f86b67cde581c0f9eb5";

    private JwtUtil jwtUtil;
    private AuthorizationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret(TEST_SECRET);
        jwtUtil.setExpire(3600);
        jwtUtil.setHeader("token");

        interceptor = new AuthorizationInterceptor();
        ReflectionTestUtils.setField(interceptor, "jwtUtil", jwtUtil);
    }

    @Test
    void preHandle_withoutLoginAnnotation_shouldPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_withoutToken_shouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = loginHandlerMethod("secured");

        SqxException ex = assertThrows(SqxException.class,
                () -> interceptor.preHandle(request, response, handlerMethod));

        assertEquals(AppAuthConstants.UNAUTHORIZED_CODE, ex.getCode());
        assertEquals(AppAuthConstants.UNAUTHORIZED_MSG, ex.getMsg());
    }

    @Test
    void preHandle_withInvalidToken_shouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + "invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = loginHandlerMethod("secured");

        SqxException ex = assertThrows(SqxException.class,
                () -> interceptor.preHandle(request, response, handlerMethod));

        assertEquals(AppAuthConstants.UNAUTHORIZED_CODE, ex.getCode());
    }

    @Test
    void preHandle_withExpiredToken_shouldReturn401() throws Exception {
        jwtUtil.setExpire(-1);
        String expiredToken = jwtUtil.generateToken(100L);
        jwtUtil.setExpire(3600);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = loginHandlerMethod("secured");

        SqxException ex = assertThrows(SqxException.class,
                () -> interceptor.preHandle(request, response, handlerMethod));

        assertEquals(AppAuthConstants.UNAUTHORIZED_CODE, ex.getCode());
    }

    @Test
    void preHandle_withBearerToken_shouldPass() throws Exception {
        String token = jwtUtil.generateToken(42L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, JwtUtil.BEARER_PREFIX + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = loginHandlerMethod("secured");

        assertTrue(interceptor.preHandle(request, response, handlerMethod));
        assertEquals(42L, request.getAttribute(AuthorizationInterceptor.USER_KEY));
    }

    @Test
    void preHandle_withLegacyTokenHeader_shouldPass() throws Exception {
        String token = jwtUtil.generateToken(7L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = loginHandlerMethod("secured");

        assertTrue(interceptor.preHandle(request, response, handlerMethod));
        assertEquals(7L, request.getAttribute(AuthorizationInterceptor.USER_KEY));
    }

    private HandlerMethod loginHandlerMethod(String methodName) throws NoSuchMethodException {
        Method method = TestController.class.getMethod(methodName);
        return new HandlerMethod(new TestController(), method);
    }

    static class TestController {
        @Login
        public void secured() {
        }

        public void open() {
        }
    }
}
