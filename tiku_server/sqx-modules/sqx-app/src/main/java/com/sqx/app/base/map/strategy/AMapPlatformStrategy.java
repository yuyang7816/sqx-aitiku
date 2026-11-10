package com.sqx.app.base.map.strategy;

import com.sqx.app.base.map.domain.vo.AddressVo;

/**
 * @author chengming
 * @since 2025/8/11
 *
 * 高德地图
 */
public class AMapPlatformStrategy implements IMapPlatformStrategy {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        throw new UnsupportedOperationException("高德地图暂不支持");
    }
}
