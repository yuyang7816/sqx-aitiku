package com.sqx.app.base.articleCms.support;

import com.sqx.common.redis.utils.ConfigUtil;
import org.springframework.stereotype.Component;

/**
 * 协议 CMS 配置值读取（封装 ConfigUtil，便于单元测试 Mock）
 */
@Component
public class ArticleConfigValueReader {

    public String getValueOrDefault(String paramKey) {
        return ConfigUtil.getConfigValueOrDefault(paramKey);
    }
}
