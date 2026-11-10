package com.sqx.app.base.map.service.impl;

import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.factory.MapFactory;
import com.sqx.app.base.map.service.MapService;
import com.sqx.app.base.map.strategy.IMapPlatformStrategy;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author chengming
 * @since 2025/7/22
 */
@Slf4j
@Service
public class MapServiceImpl implements MapService {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        String mapPlatform = ConfigUtil.getConfigValue("map_platform");

        MapFactory mapFactory = MapFactory.getInstance();
        IMapPlatformStrategy platform = mapFactory.getPlatform(mapPlatform);

        return platform.reverseGeocode(lat, lng);
    }
}
