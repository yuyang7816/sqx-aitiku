package com.sqx.app.referral.withdraw.support;

import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import org.apache.commons.lang3.StringUtils;

/**
 * 实名认证辅助（身份证号校验、脱敏、认证状态判断）
 */
public final class RealNameSupport {

    private static final int VERIFIED = 1;

    private RealNameSupport() {
    }

    /**
     * 校验身份证号格式（15 位或 18 位，末位可为 X）
     */
    public static void validateIdCardNo(String idCardNo) {
        if (StringUtils.isBlank(idCardNo)) {
            throw new IllegalArgumentException("身份证号不能为空");
        }
        String trimmed = idCardNo.trim();
        if (!trimmed.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$")
                && !trimmed.matches("^[1-9]\\d{7}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$")) {
            throw new IllegalArgumentException("请输入正确的身份证号");
        }
    }

    /**
     * 是否已完成实名认证（身份证流程）
     */
    public static boolean isRealNameVerified(WithdrawalAccount account) {
        if (account == null) {
            return false;
        }
        if (!Integer.valueOf(VERIFIED).equals(account.getRealNameVerified())) {
            return false;
        }
        return StringUtils.isNotBlank(account.getRealName())
                && StringUtils.isNotBlank(account.getIdCardNo());
    }

    /**
     * 提现账户是否已绑定完成（C 端简化绑卡：微信收款码 / 支付宝账号+姓名）
     *
     * @param account              提现账户
     * @param withdrawalAccountType withdrawal_account.type：1 支付宝 2 微信
     */
    public static boolean isWithdrawBindingReady(WithdrawalAccount account, int withdrawalAccountType) {
        if (account == null) {
            return false;
        }
        if (withdrawalAccountType == 2) {
            return StringUtils.isNotBlank(account.getAccountNumber());
        }
        if (withdrawalAccountType == 1) {
            return StringUtils.isNotBlank(account.getAccountNumber())
                    && StringUtils.isNotBlank(account.getAccountName());
        }
        return isRealNameVerified(account);
    }

    /**
     * 身份证号脱敏展示
     */
    public static String maskIdCardNo(String idCardNo) {
        if (StringUtils.isBlank(idCardNo)) {
            return "";
        }
        String trimmed = idCardNo.trim();
        if (trimmed.length() <= 8) {
            return "****";
        }
        int maskLen = trimmed.length() - 8;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLen; i++) {
            mask.append('*');
        }
        return trimmed.substring(0, 4) + mask + trimmed.substring(trimmed.length() - 4);
    }
}
