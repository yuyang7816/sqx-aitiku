package com.sqx.common.pay.channel.wechat.enums;

import com.sqx.common.core.exception.BizException;

/**
 * @author chengming
 * @since 2025/4/22
 */
public enum WxTradeType {

    JSAPI("JSAPI", "公众号支付"),
    NATIVE("NATIVE", "原生扫码支付"),
    APP("APP", "app支付"),
    H5("H5", "H5支付"),
    ;

    private final String code;
    private final String desc;

    WxTradeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WxTradeType fromCode(String code) {
        for (WxTradeType tradeType : WxTradeType.values()) {
            if (tradeType.code.equals(code)) {
                return tradeType;
            }
        }

        throw new BizException("不支持的交易类型");
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
