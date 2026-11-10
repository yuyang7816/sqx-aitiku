package com.sqx.common.pay.enums;

import com.sqx.common.core.exception.BizException;

import java.util.Objects;

/**
 * @author chengming
 * @since 2026/1/9
 */
public enum PayChannel {
    /**
     * 支付宝
     */
    ALI("ali"),

    /**
     * 微信
     */
    WX("wx"),
    ;

    private final String value;

    PayChannel(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PayChannel fromCode(String code) {
        for (PayChannel channel : PayChannel.values()) {
            if (Objects.equals(channel.getValue(), code)) {
                return channel;
            }
        }

        throw new BizException("不支持的支付渠道");
    }
}
