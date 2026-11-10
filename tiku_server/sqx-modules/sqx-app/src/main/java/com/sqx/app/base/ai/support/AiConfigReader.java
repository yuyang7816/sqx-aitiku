package com.sqx.app.base.ai.support;

import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AiConfigReader {

    public String getPlatform() {
        String platform = ConfigUtil.getConfigValueOrDefault("ai_platform");
        return platform == null ? "local_template" : platform;
    }

    public int getDailyLimit() {
        String value = ConfigUtil.getConfigValueOrDefault("ai_daily_limit");
        if (value == null || value.trim().isEmpty()) {
            return 5;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed <= 0 ? 5 : parsed;
        } catch (NumberFormatException ignored) {
            return 5;
        }
    }

    public String getDoubaoApiKey() {
        return ConfigUtil.getConfigValueOrDefault("ai_doubao_api_key");
    }

    public String getDoubaoModel() {
        String model = ConfigUtil.getConfigValueOrDefault("ai_doubao_model");
        return model == null || model.trim().isEmpty() ? "doubao-pro-32k" : model.trim();
    }

    public String getDoubaoEndpoint() {
        String endpoint = ConfigUtil.getConfigValueOrDefault("ai_doubao_endpoint");
        return endpoint == null || endpoint.trim().isEmpty()
                ? "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
                : endpoint.trim();
    }

    public List<String> getSensitiveWords() {
        String value = ConfigUtil.getConfigValueOrDefault("ai_sensitive_words");
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        String[] parts = value.split("[,;\\n\\r]+");
        List<String> words = new ArrayList<>();
        for (String part : parts) {
            String word = StringUtils.trimToEmpty(part);
            if (StringUtils.isNotBlank(word)) {
                words.add(word);
            }
        }
        return words;
    }
}