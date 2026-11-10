package com.sqx.app.base.utils.map;

import lombok.Data;

import java.util.Map;

/**
 * @author chengming
 * @since 2025/5/13
 */
@Data
public class AddressResult {

    /**
     * 地址id（仅限谷歌地图）
     */
    private String placeId;

    /**
     * 格式化地址
     */
    private String formattedAddress;

    /**
     * 地址信息
     */
    private Map<String, String> addressMap;
}
