package com.sqx.app.referral.commission.service;

import com.sqx.app.vip.pay.domain.entity.VipOrder;

/**
 * 会员支付成功后触发上级推广佣金
 */
public interface ReferralCommissionTriggerService {

    /**
     * 下级会员订单支付成功后，为上级计算佣金并写入钱包流水
     *
     * @param order 已支付会员订单
     */
    void triggerOnVipPaySuccess(VipOrder order);
}
