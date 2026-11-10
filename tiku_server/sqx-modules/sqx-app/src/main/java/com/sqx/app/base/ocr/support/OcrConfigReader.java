package com.sqx.app.base.ocr.support;

import com.sqx.common.redis.utils.ConfigUtil;
import org.springframework.stereotype.Component;

/**
 * OCR 配置读取（便于单元测试替换）
 */
@Component
public class OcrConfigReader {

    public String getPlatform() {
        String platform = ConfigUtil.getConfigValueOrDefault("ocr_platform");
        return platform == null ? "local_sidecar" : platform;
    }

    public double getSimilarityThreshold() {
        String value = ConfigUtil.getConfigValueOrDefault("ocr_similarity_threshold");
        if (value == null || value.trim().isEmpty()) {
            return 0.08D;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ignored) {
            return 0.08D;
        }
    }

    public int getMaxResults() {
        String value = ConfigUtil.getConfigValueOrDefault("ocr_max_results");
        if (value == null || value.trim().isEmpty()) {
            return 10;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed <= 0 ? 10 : Math.min(parsed, 50);
        } catch (NumberFormatException ignored) {
            return 10;
        }
    }

    public String getBaiduApiKey() {
        return ConfigUtil.getConfigValueOrDefault("ocr_baidu_api_key");
    }

    public String getBaiduSecretKey() {
        return ConfigUtil.getConfigValueOrDefault("ocr_baidu_secret_key");
    }

    /**
     * 非会员每日 OCR 搜题次数上限（sys_config.ocr_daily_limit，默认 5）
     */
    public int getDailyLimit() {
        String value = ConfigUtil.getConfigValueOrDefault("ocr_daily_limit");
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
}
