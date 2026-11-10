package com.sqx.common.sms.domain;

import lombok.Data;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Data
public class ConvertPhone {

    /**
     * 是否国内手机号
     */
    private Boolean china;

    /**
     * 编码后的手机号
     */
    private String encodePhone;
}
