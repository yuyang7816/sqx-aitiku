package com.sqx.common.redis.config;

import com.sqx.common.redis.config.properties.RedissonProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chengming
 * @since 2024/9/29
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {
    @Value("${spring.application.name}")
    private String clientName;

    @Bean
    public RedissonClient redissonClient(RedissonProperties redisProperties) {
        log.info("初始化Redisson");
        Config config = new Config();
        config.setThreads(16);
        config.setNettyThreads(32);
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.setTransportMode(TransportMode.NIO);

        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        if (StringUtils.isNotBlank(redisProperties.getPassword())) {
            singleServerConfig.setPassword(redisProperties.getPassword());
        } else {
            singleServerConfig.setPassword(null);
        }
        singleServerConfig.setDatabase(redisProperties.getDatabase());
        singleServerConfig.setClientName(clientName);
        singleServerConfig.setIdleConnectionTimeout(redisProperties.getIdleConnectionTimeout());
        singleServerConfig.setConnectTimeout(redisProperties.getConnectTimeout());
        singleServerConfig.setTimeout(redisProperties.getTimeout());
        singleServerConfig.setRetryAttempts(redisProperties.getRetryAttempts());
        singleServerConfig.setRetryInterval(redisProperties.getRetryInterval());
        singleServerConfig.setSubscriptionsPerConnection(redisProperties.getSubscriptionsPerConnection());
        singleServerConfig.setSubscriptionConnectionMinimumIdleSize(redisProperties.getSubscriptionConnectionMinimumIdleSize());
        singleServerConfig.setConnectionPoolSize(redisProperties.getConnectionPoolSize());
        singleServerConfig.setConnectionMinimumIdleSize(redisProperties.getConnectionMinimumIdleSize());
        singleServerConfig.setConnectionPoolSize(redisProperties.getConnectionPoolSize());
        singleServerConfig.setDnsMonitoringInterval(redisProperties.getDnsMonitoringInterval());

        return Redisson.create(config);
    }
}
