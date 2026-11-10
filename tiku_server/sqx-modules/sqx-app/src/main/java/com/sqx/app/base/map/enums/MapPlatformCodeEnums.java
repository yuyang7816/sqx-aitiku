package com.sqx.app.base.map.enums;

/**
 * @author chengming
 * @since 2025/8/11
 */
public enum MapPlatformCodeEnums {

    TIANDITU("tianditu", "天地图"),
    TENCENT("tencent", "腾讯地图"),
    AMAP("amap", "高德地图"),
    GOOGLE("google", "谷歌地图")
    ;

    private final String code;
    private final String name;

    MapPlatformCodeEnums(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
