package com.sqx.common.redis.utils;

/**
 * Redis所有Keys
 *
 */
public class RedisKeys {

    public static String getSysConfigKey(String key){
        return "sys:config:" + key;
    }

    /**
     * common_info 缓存key
     */
    public static final String COMMON_INFO_KEY = "common::info::";

    /**
     * sys_config 缓存key
     */
    public static final String SYS_CONFIG_KEY = "sys::config::";
}
