package com.sqx.common.pay.channel.ali.enums;

/**
 * @author chengming
 * @since 2025/4/21
 */
public enum AliSandbox {

    YES("1", "开启沙箱模式"),
    NO("2", "关闭沙箱模式"),
    ;

    private final String code;
    private final String desc;

    AliSandbox(String code, String desc) {
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
