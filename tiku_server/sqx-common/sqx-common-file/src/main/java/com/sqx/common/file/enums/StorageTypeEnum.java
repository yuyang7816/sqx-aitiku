package com.sqx.common.file.enums;

/**
 * @author chengming
 * @since 2024/10/25
 */
public enum StorageTypeEnum {
    LOCAL("local", "本地"),
    ALIOSS("ali", "ali OSS")
    ;

    private final String code;
    private final String desc;

    StorageTypeEnum(String code, String desc) {
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
