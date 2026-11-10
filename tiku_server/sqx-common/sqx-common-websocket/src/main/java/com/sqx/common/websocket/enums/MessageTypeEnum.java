package com.sqx.common.websocket.enums;

/**
 * @author chengming
 * @since 2024/12/31
 */
public enum MessageTypeEnum {

    PING("ping", "ping消息"),
    TEXT("text", "文本消息"),
    BINARY("binary", "二进制消息"),
    ;

    private final String code;
    private final String desc;

    MessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String code() {
        return code;
    }

    public String desc() {
        return desc;
    }
}
