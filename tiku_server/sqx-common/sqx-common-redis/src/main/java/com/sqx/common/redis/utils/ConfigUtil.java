package com.sqx.common.redis.utils;

import com.alibaba.fastjson2.JSONObject;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author chengming
 * @since 2025/5/14
 */
@Slf4j
public class ConfigUtil {
    public static String getConfigValue(String paramKey) {
        String key = RedisKeys.SYS_CONFIG_KEY + paramKey;
        if (!RedisUtil.hasKey(key)) {
            log.error("配置不存在: {}", paramKey);
            throw new BizException("配置不存在");
        }

        String config = readConfigJsonString(key);
        if (StringUtils.isBlank(config)) {
            log.error("配置不存在: {}", paramKey);
            throw new BizException("配置不存在");
        }
        try {
            JSONObject configJson = JSONObject.parseObject(config);
            return configJson.getString("paramValue");
        } catch (Exception e) {
            log.error("配置解析失败: {}", paramKey, e);
            throw new BizException("配置不存在");
        }
    }

    public static String getConfigValueOrDefault(String paramKey) {
        String key = RedisKeys.SYS_CONFIG_KEY + paramKey;
        if (!RedisUtil.hasKey(key)) {
            return null;
        }

        String config = readConfigJsonString(key);
        if (StringUtils.isBlank(config)) {
            return null;
        }
        try {
            JSONObject configJson = JSONObject.parseObject(config);
            return configJson == null ? null : configJson.getString("paramValue");
        } catch (Exception e) {
            log.warn("配置解析失败 paramKey={}: {}", paramKey, e.getMessage());
            return null;
        }
    }

    private static String readConfigJsonString(String key) {
        Object cached = RedisUtil.getCacheObject(key);
        if (cached != null) {
            if (cached instanceof String) {
                return (String) cached;
            }
            return String.valueOf(cached);
        }
        return RedisUtil.getCacheString(key);
    }
}
