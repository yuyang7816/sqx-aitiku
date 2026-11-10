package com.sqx.app.base.map.strategy;

import com.sqx.app.base.map.domain.vo.AddressVo;

/**
 * @author chengming
 * @since 2025/8/11
 */
public interface IMapPlatformStrategy {

    /**
     * 反向地理编码（根据经纬度获取地址信息）
     * @param lat 维度
     * @param lng 经度
     */
    AddressVo reverseGeocode(String lat, String lng);
}
