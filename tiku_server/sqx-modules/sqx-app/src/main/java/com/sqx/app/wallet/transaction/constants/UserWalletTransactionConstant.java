package com.sqx.app.wallet.transaction.constants;

/**
 * 用户钱包收益流水常量（GET /app/userWalletTransaction）
 */
public final class UserWalletTransactionConstant {

    private UserWalletTransactionConstant() {
    }

    /** 资金流向：收入 */
    public static final int DIRECTION_INCOME = 1;

    /** 状态：待处理（待结算） */
    public static final int STATUS_PENDING = 1;

    /** 状态：已完成（已到账） */
    public static final int STATUS_COMPLETED = 2;

    /** 展示文案：待结算 */
    public static final String LABEL_PENDING = "待结算";

    /** 展示文案：已到账 */
    public static final String LABEL_SETTLED = "已到账";

    /** 默认分页大小 */
    public static final long DEFAULT_LIMIT = 10L;
}
