package com.sqx.common.core.exception;

import com.sqx.common.core.enums.IErrorCode;

/**
 * @author chengming
 * @since 2025/4/17
 *
 * 支付异常
 */
public class PayException extends RuntimeException{
    private static final long serialVersionUID = -1L;

    private IErrorCode errorCode;

    public PayException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PayException(String message) {
        super(message);
    }

    public PayException(Throwable cause) {
        super(cause);
    }

    public PayException(String message, Throwable cause) {
        super(message, cause);
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(IErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
