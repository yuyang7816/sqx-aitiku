package com.sqx.common.redis.utils;

import com.sqx.common.redis.config.properties.RedissonProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Redis key 前缀，在 RedisUtil 封装层统一隔离多项目数据
 */
@Slf4j
@Component
public class RedisKeyPrefix {

    private static String prefix = "";

    public RedisKeyPrefix(RedissonProperties properties,
                          @Value("${spring.application.name:}") String applicationName) {
        String configured = properties.getKeyPrefix();
        if (configured != null) {
            prefix = normalize(configured);
        } else if (StringUtils.isNotBlank(applicationName)) {
            prefix = applicationName + ":";
        }
        if (StringUtils.isNotBlank(prefix)) {
            log.info("Redis key 前缀: {}", prefix);
        }
    }

    public static String key(String rawKey) {
        if (StringUtils.isBlank(prefix) || StringUtils.isBlank(rawKey)) {
            return rawKey;
        }
        return prefix + rawKey;
    }

    public static String pattern(String rawPattern) {
        return key(rawPattern);
    }

    private static String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.endsWith(":") ? value : value + ":";
    }
}
