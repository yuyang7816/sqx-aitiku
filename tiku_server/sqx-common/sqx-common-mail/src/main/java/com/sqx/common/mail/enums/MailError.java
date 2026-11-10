package com.sqx.common.mail.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * @author chengming
 * @since 2025/6/12
 */
public enum MailError implements IErrorCode {

    SEND_FAIL(550001, "邮件发送失败"),
    ;

    MailError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;
    private String message;

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
