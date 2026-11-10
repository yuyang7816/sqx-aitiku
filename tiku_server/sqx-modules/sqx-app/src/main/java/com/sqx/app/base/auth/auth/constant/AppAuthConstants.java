package com.sqx.app.base.auth.auth.constant;

/**
 * APP 端鉴权常量（/app/** 接口 Token 校验）
 */
public final class AppAuthConstants {

    /**
     * 标准 Authorization 请求头
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer Token 前缀，完整格式：Authorization: Bearer {token}
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * 未授权响应体 code，与 uniapp 端 httpRequest.js 约定一致
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 未授权统一提示（无 token / token 无效 / token 过期）
     */
    public static final String UNAUTHORIZED_MSG = "暂未登录或token已经过期";

    private AppAuthConstants() {
    }
}
