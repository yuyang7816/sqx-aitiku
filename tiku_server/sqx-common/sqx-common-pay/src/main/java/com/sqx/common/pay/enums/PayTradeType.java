package com.sqx.common.pay.enums;

/**
 * @author chengming
 * @since 2026/1/9
 */
public enum PayTradeType {

    JSAPI("JSAPI"),
    NATIVE("NATIVE"),
    APP("APP"),
    H5("H5"),
    ;

    private final String code;

    PayTradeType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
