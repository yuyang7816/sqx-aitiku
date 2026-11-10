package com.sqx.app.base.customerConfig.support;

import com.sqx.common.redis.utils.ConfigUtil;
import org.springframework.stereotype.Component;

/**
 * 客服配置值读取（封装 ConfigUtil，便于单元测试 Mock）
 */
@Component
public class CustomerConfigValueReader {

    public String getValueOrDefault(String paramKey) {
        return ConfigUtil.getConfigValueOrDefault(paramKey);
    }
}
