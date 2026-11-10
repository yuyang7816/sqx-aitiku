package com.sqx.app.vip.renew.support;

import com.sqx.common.redis.utils.ConfigUtil;
import org.springframework.stereotype.Component;

/**
 * 会员续费提醒配置：到期前 N 天推送（sys_config.vip_renew_notice_days）
 */
@Component
public class VipRenewConfigReader {

    private static final int DEFAULT_NOTICE_DAYS = 3;

    public int getNoticeDaysBeforeExpire() {
        String value = ConfigUtil.getConfigValueOrDefault("vip_renew_notice_days");
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_NOTICE_DAYS;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed <= 0 ? DEFAULT_NOTICE_DAYS : parsed;
        } catch (NumberFormatException ignored) {
            return DEFAULT_NOTICE_DAYS;
        }
    }
}
