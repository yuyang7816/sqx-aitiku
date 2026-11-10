package com.sqx.common.pay.channel.ali.enums;

/**
 * @author chengming
 * @since 2025/4/21
 * 支付宝支付模式枚举
 */
public enum AliMode {
    PUBLIC_KEY("1", "普通公钥模式"),
    CERT("2", "证书模式"),
    ;

    private final String code;
    private final String desc;

    AliMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
