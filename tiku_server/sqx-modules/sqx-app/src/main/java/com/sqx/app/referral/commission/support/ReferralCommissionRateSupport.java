package com.sqx.app.referral.commission.support;

import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.constants.ReferralConstant;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 推广佣金比例：全局默认 + 用户单独覆盖
 */
public final class ReferralCommissionRateSupport {

    private static final BigDecimal DEFAULT_LEVEL1 = new BigDecimal("0.10");
    private static final BigDecimal DEFAULT_LEVEL2 = new BigDecimal("0.05");

    private ReferralCommissionRateSupport() {
    }

    public static BigDecimal resolveLevel1Rate(UserEntity user, ISysConfigService sysConfigService) {
        BigDecimal userRate = user == null ? null : user.getCommissionRateLevel1();
        if (isPositiveRate(userRate)) {
            return userRate;
        }
        return resolveGlobalRate(sysConfigService, ReferralConstant.commission.SYS_COMMISSION_RATE_LEVEL1,
                ReferralConstant.commission.SYS_COMMISSION_RATE, DEFAULT_LEVEL1);
    }

    public static BigDecimal resolveLevel2Rate(UserEntity user, ISysConfigService sysConfigService) {
        BigDecimal userRate = user == null ? null : user.getCommissionRateLevel2();
        if (isPositiveRate(userRate)) {
            return userRate;
        }
        return resolveGlobalRate(sysConfigService, ReferralConstant.commission.SYS_COMMISSION_RATE_LEVEL2,
                null, DEFAULT_LEVEL2);
    }

    private static BigDecimal resolveGlobalRate(ISysConfigService sysConfigService, String primaryKey,
                                                String fallbackKey, BigDecimal defaultValue) {
        BigDecimal rate = parseRate(sysConfigService.getConfigValue(primaryKey));
        if (isPositiveRate(rate)) {
            return rate;
        }
        if (StringUtils.isNotBlank(fallbackKey)) {
            rate = parseRate(sysConfigService.getConfigValue(fallbackKey));
            if (isPositiveRate(rate)) {
                return rate;
            }
        }
        return defaultValue;
    }

    private static BigDecimal parseRate(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean isPositiveRate(BigDecimal rate) {
        return rate != null && rate.compareTo(BigDecimal.ZERO) > 0;
    }
}
