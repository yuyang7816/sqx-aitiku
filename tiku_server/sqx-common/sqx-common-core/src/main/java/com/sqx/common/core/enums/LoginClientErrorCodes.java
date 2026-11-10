package com.sqx.common.core.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户端登录/验证码相关错误码集合（对应 HTTP 400 响应）
 */
public final class LoginClientErrorCodes {

    private static final Set<Integer> BAD_REQUEST_CODES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            350001,
            350002,
            350003,
            350004,
            350005,
            350010,
            350011,
            350012,
            350022,
            350017,
            350018,
            350019,
            SystemErrorCode.VALIDATE_FAILED.getCode()
    )));

    private LoginClientErrorCodes() {
    }

    /**
     * 登录链路业务错误是否应返回 HTTP 400
     */
    public static boolean isBadRequest(Integer code) {
        return code != null && BAD_REQUEST_CODES.contains(code);
    }

    /**
     * 供测试与文档核对：登录相关 distinct 错误码集合
     */
    public static Set<Integer> allCodes() {
        return BAD_REQUEST_CODES;
    }
}
