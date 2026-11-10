package com.sqx.common.core.enums;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 登录错误码 distinct 与 HTTP 400 映射校验
 */
class LoginClientErrorCodesTest {

    @Test
    void allLoginErrorCodes_shouldBeDistinct() {
        Set<Integer> codes = LoginClientErrorCodes.allCodes();
        assertEquals(codes.size(), new HashSet<>(codes).size(),
                "登录错误码存在重复: " + codes);
    }

    @Test
    void loginBizErrors_shouldMapToBadRequest() {
        assertTrue(LoginClientErrorCodes.isBadRequest(350001));
        assertTrue(LoginClientErrorCodes.isBadRequest(350002));
        assertTrue(LoginClientErrorCodes.isBadRequest(350003));
        assertTrue(LoginClientErrorCodes.isBadRequest(350004));
        assertTrue(LoginClientErrorCodes.isBadRequest(350005));
        assertTrue(LoginClientErrorCodes.isBadRequest(350010));
        assertTrue(LoginClientErrorCodes.isBadRequest(350011));
        assertTrue(LoginClientErrorCodes.isBadRequest(350012));
        assertTrue(LoginClientErrorCodes.isBadRequest(350017));
        assertTrue(LoginClientErrorCodes.isBadRequest(350018));
        assertTrue(LoginClientErrorCodes.isBadRequest(350019));
        assertTrue(LoginClientErrorCodes.isBadRequest(SystemErrorCode.VALIDATE_FAILED.getCode()));
    }

    @Test
    void nonLoginErrors_shouldNotMapToBadRequest() {
        assertFalse(LoginClientErrorCodes.isBadRequest(350006));
        assertFalse(LoginClientErrorCodes.isBadRequest(350007));
        assertFalse(LoginClientErrorCodes.isBadRequest(350013));
        assertFalse(LoginClientErrorCodes.isBadRequest(500));
        assertFalse(LoginClientErrorCodes.isBadRequest(401));
    }
}
