package com.sqx.app.referral.withdraw.constants;

/**
 * 提现渠道常量（withdraw_record.account_type：1=微信零钱 2=支付宝）
 */
public final class WithdrawChannelConstants {

    /** 微信零钱 */
    public static final int WECHAT = 1;

    /** 支付宝 */
    public static final int ALIPAY = 2;

    /** 提现记录状态：待审核（发起提现） */
    public static final int STATUS_PENDING = 1;

    /** 提现记录状态：审核通过/打款成功 */
    public static final int STATUS_SUCCESS = 2;

    /** 提现记录状态：审核拒绝 */
    public static final int STATUS_REJECTED = 3;

    /** withdrawal_account.type 与 withdraw_record.account_type 映射：微信 */
    public static final int ACCOUNT_TYPE_WECHAT = 2;

    /** withdrawal_account.type 与 withdraw_record.account_type 映射：支付宝 */
    public static final int ACCOUNT_TYPE_ALIPAY = 1;

    private WithdrawChannelConstants() {
    }
}
