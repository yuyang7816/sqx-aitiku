package com.sqx.app.base.utils;

import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.RedisKeys;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.system.db.entity.SysConfigEntity;

import java.util.Objects;

/**
 * @author chengming
 * @since 2025/5/14
 * 系统配置工具类
 */
public class SysConfigUtil {

    /**
     * 获取系统配置
     * @param paramKey key
     * @return
     */
    public static SysConfigEntity getConfig(String paramKey) {
        String key = RedisKeys.SYS_CONFIG_KEY + paramKey;
        if (!RedisUtil.hasKey(key)) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        String configJson = RedisUtil.getCacheObject(key);
        SysConfigEntity sysConfig = JSONObject.parseObject(configJson, SysConfigEntity.class);
        if (Objects.isNull(sysConfig)) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        return sysConfig;
    }

    /**
     * 获取系统配置的值
     * @param paramKey key
     * @return
     */
    public static String getConfigValue(String paramKey) {
        SysConfigEntity sysConfig = getConfig(paramKey);
        return sysConfig.getParamValue();
    }
}
