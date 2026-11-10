package com.sqx.app.base.map.factory;

import com.sqx.app.base.map.enums.MapPlatformCodeEnums;
import com.sqx.app.base.map.strategy.*;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengming
 * @since 2025/8/11
 */
public class MapFactory {

    private static final Map<String, IMapPlatformStrategy> PLATFORM_MAP = new ConcurrentHashMap<>();

    static {
        PLATFORM_MAP.put(MapPlatformCodeEnums.TIANDITU.getCode(), new TianDiTuMapPlatformStrategy());
        PLATFORM_MAP.put(MapPlatformCodeEnums.TENCENT.getCode(), new TencentMapPlatformStrategy());
        PLATFORM_MAP.put(MapPlatformCodeEnums.AMAP.getCode(), new AMapPlatformStrategy());
        PLATFORM_MAP.put(MapPlatformCodeEnums.GOOGLE.getCode(), new GoogleMapPlatformStrategy());
    }

    public IMapPlatformStrategy getPlatform(String code) {
        if (StringUtils.isBlank(code)) {
            throw new BizException("地图code不能为空");
        }

        return PLATFORM_MAP.get(code);
    }

    private MapFactory() {}

    /**
     * 获取工厂的单例实例
     *
     * @return MapFactory单例
     */
    public static MapFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 单例持有类（延迟加载，线程安全）
     */
    private static class SingletonHolder {
        private static final MapFactory INSTANCE = new MapFactory();
    }
}
