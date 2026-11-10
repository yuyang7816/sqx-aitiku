package com.sqx.common.core.exception;

import com.sqx.common.core.enums.IErrorCode;

/**
 * @author chengming
 * @since 2024/7/11
 * 自定义业务异常
 */
public class BizException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    private IErrorCode errorCode;

    public BizException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public IErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(IErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
