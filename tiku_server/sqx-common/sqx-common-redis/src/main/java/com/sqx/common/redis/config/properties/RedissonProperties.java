package com.sqx.common.redis.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chengming
 * @since 2024/9/29
 */
@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {
    /**
     * redis地址
     */
    private String host;

    /**
     * redis端口号
     */
    private Integer port;

    /**
     * redis密码
     */
    private String password;

    /**
     * 数据库
     */
    private Integer database;

    /**
     * Redis key 前缀，多项目共用同一实例时隔离数据，建议以冒号结尾（如 chongwushejiao:）
     * 未配置时默认使用 spring.application.name
     */
    private String keyPrefix;

    private Integer idleConnectionTimeout;

    private Integer connectTimeout;

    private Integer timeout;

    private Integer retryAttempts;

    private Integer retryInterval;

    private Integer subscriptionsPerConnection;

    private Integer subscriptionConnectionMinimumIdleSize;

    private Integer subscriptionConnectionPoolSize;

    private Integer connectionMinimumIdleSize;

    private Integer connectionPoolSize;

    private Integer dnsMonitoringInterval;

    private Integer threads;

    private Integer nettyThreads;
}
