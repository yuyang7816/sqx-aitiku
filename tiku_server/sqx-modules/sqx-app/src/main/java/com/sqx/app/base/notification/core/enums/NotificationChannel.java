package com.sqx.app.base.notification.core.enums;

/**
 * @author chengming
 * @since 2025/9/9
 *
 * 消息推送方式
 */
public enum NotificationChannel {
    INSTATION("instation", "站内信"),
    MP("mp", "公众号"),
    MA("ma", "小程序"),
    APP("app", "app"),
    ;

    private final String code;
    private final String desc;

    NotificationChannel(String code, String desc) {
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
