package com.sqx.common.sms.enums;

/**
 * @author chengming
 * @since 2025/6/11
 */
public enum SmsProviderEnum {
    MOCK("0", "Mock（开发环境）"),
    TENCENT("1", "腾讯云短信平台"),
    DXB("3", "短信宝"),
    ALIYUN("2", "阿里云短信平台"),
    ;

    private final String code;
    private final String desc;

    SmsProviderEnum(String code, String desc) {
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
