package com.sqx.app.referral.user.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;

import java.math.BigDecimal;

/**
 * <p>
 * 用户佣金 服务类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:53
 */
public interface IReferralUserCommissionService extends IService<ReferralUserCommission> {

    void incr(Long userId, BigDecimal commission);

    ReferralUserCommission getByUserId(Long userId);

    void decr(Long userId, BigDecimal amount);

    /**
     * 邀请人收益到账
     * @param orderId 订单id
     * @param userId 邀请人用户id
     * @param commissionRate 佣金比例
     * @param amount 初始金额
     */
    void incrInvite(Long orderId, Long userId, BigDecimal commissionRate, BigDecimal amount);

    /**
     * 订单金额到账
     * @param userId 用户ID
     * @param orderId 订单号
     * @param amount 订单金额
     */
    void saveOrder(Long userId, Long orderId, BigDecimal amount);
}
