package com.sqx.app.base.verifyCode.constans;

/**
 * @author chengming
 * @since 2025/4/22
 */
public class VerifyCodeConstant {

    /**
     * 缓存key
     */
    public static final String CACHE_KEY = "verify_code::";

    /**
     * 发送间隔（秒）
     */
    public static final int SEND_INTERVAL_SECONDS = 60;

    /**
     * 用户端登录默认业务码
     */
    public static final String DEFAULT_BIZ_CODE = "login";


    /* ====验证码消息体==== */
    public static final String REGISTER_CONTENT = "【%s】验证码: %s，此验证码可用于登录或注册，10分钟内有效，如非您本人操作，可忽略本条消息";
    public static final String FORGET_CONTENT = "【%s】验证码: %s，您正在执行找回密码操作，10分钟内有效，如非您本人操作，可忽略本条消息";
    public static final String BIND_CONTENT = "【%s】验证码: %s，您正在执行绑定手机号操作，10分钟内有效，如非您本人操作，可忽略本条消息";
}
