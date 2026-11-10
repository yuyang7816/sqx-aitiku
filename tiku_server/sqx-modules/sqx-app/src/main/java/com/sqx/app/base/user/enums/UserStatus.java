package com.sqx.app.base.user.enums;

/**
 * @author chengming
 * @since 2025/12/19
 */
public enum UserStatus {

    ENABLE(1, "正常"),
    DISABLE(2, "禁用"),
    ;

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
