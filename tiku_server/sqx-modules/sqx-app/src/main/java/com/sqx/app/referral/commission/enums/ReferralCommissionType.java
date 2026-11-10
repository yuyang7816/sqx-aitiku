package com.sqx.app.referral.commission.enums;

/**
 * @author chengming
 * @since 2026/2/6
 */
public enum ReferralCommissionType {

    /**
     * 提现
     */
    WITHDRAW(0),

    /**
     * 订单佣金
     */
    ORDER(1);

    private Integer value;

    ReferralCommissionType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
