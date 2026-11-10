package com.sqx.app.base.user.enums;

/**
 * @author chengming
 * @since 2025/12/19
 */
public enum UserType {
    USER(1, "普通用户"),
    ;

    private final Integer code;
    private final String desc;

    UserType(Integer code, String desc) {
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
