package com.sqx.app.referral.withdraw.support;

import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 提现金额校验（与 uniapp withdraw-rules 对齐：50 元步进，单笔 50～5000）
 */
public final class WithdrawAmountValidator {

    public static final BigDecimal DEFAULT_MIN = new BigDecimal("50");
    public static final BigDecimal DEFAULT_MAX = new BigDecimal("5000");
    public static final BigDecimal STEP = new BigDecimal("50");

    private WithdrawAmountValidator() {
    }

    public static void validate(BigDecimal amount, BigDecimal balance, String minConfig, String maxConfig) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("提现金额必须大于0");
        }

        BigDecimal minAmount = parseConfig(minConfig, DEFAULT_MIN);
        BigDecimal maxAmount = parseConfig(maxConfig, DEFAULT_MAX);

        if (amount.compareTo(minAmount) < 0) {
            throw new BizException("最小提现金额为" + minAmount.stripTrailingZeros().toPlainString() + "元");
        }
        if (amount.compareTo(maxAmount) > 0) {
            throw new BizException("单日提现不超过" + maxAmount.stripTrailingZeros().toPlainString() + "元");
        }

        BigDecimal cents = amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal stepCents = STEP.multiply(new BigDecimal("100"));
        if (cents.remainder(stepCents).compareTo(BigDecimal.ZERO) != 0) {
            throw new BizException("提现金额需为" + STEP.stripTrailingZeros().toPlainString() + "元的整数倍");
        }

        BigDecimal normalizedBalance = balance == null ? BigDecimal.ZERO : balance;
        if (amount.compareTo(normalizedBalance) > 0) {
            throw new BizException("提现金额不能超过可提现余额");
        }
    }

    private static BigDecimal parseConfig(String configValue, BigDecimal defaultValue) {
        if (StringUtils.isBlank(configValue)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(configValue.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
