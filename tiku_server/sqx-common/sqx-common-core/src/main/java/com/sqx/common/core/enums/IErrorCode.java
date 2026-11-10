package com.sqx.common.core.enums;

/**
 * @author chengming
 * @since 2024/7/11
 */
public interface IErrorCode {
    /**
     * 错误码
     * @return
     */
    Integer getCode();

    /**
     *错误信息
     * @return
     */
    String getMessage();
}
