package com.sqx.common.sms.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * @author chengming
 * @since 2025/6/11
 */
public enum SmsError implements IErrorCode {
    PASSWORD_ERROR(450001, "短信宝密码错误"),
    USERNAME_ERROR(450002, "短信宝账号不存在"),
    BALANCE_ERROR(450003, "短信宝余额不足"),
    IP_LIMITATION(450004, "短信宝IP地址限制"),
    SENSITIVE_WORDS(450005, "短信内容含有敏感词"),
    PHONE_ERROR(450005, "短信手机号码不正确"),
    NOT_SUPPORT_PROVIDER(450006, "不支持的短信平台"),
    SMS_SEND_ERROR(450007, "短信发送失败"),
    ;

    private final Integer code;
    private final String message;

    SmsError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
