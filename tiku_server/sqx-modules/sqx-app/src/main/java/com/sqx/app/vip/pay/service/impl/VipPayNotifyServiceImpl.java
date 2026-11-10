package com.sqx.app.vip.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.service.ReferralCommissionTriggerService;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.vip.pay.enums.PayError;
import com.sqx.app.vip.pay.service.VipPayNotifyService;
import com.sqx.app.vip.rules.support.ActivationQuotaReset;
import com.sqx.app.vip.rules.support.MemberRulesSupport;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 微信支付回调成功后开通/续费 VIP
 */
@Slf4j
@Service
public class VipPayNotifyServiceImpl implements VipPayNotifyService {

    @Resource
    private IVipOrderService iVipOrderService;
    @Resource
    private IUserService iUserService;
    @Resource
    private ReferralCommissionTriggerService referralCommissionTriggerService;
    @Resource
    private MemberRulesSupport memberRulesSupport;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onWechatPaySuccess(String outTradeNo, String tradeNo) {
        VipOrder order = iVipOrderService.getOne(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getOrderNo, outTradeNo)
                .last("LIMIT 1"));
        if (order == null) {
            log.error("支付回调订单不存在 outTradeNo={}", outTradeNo);
            throw new BizException(PayError.ORDER_NOT_FOUND);
        }

        if (Objects.equals(order.getPayStatus(), VipOrder.STATUS_PAID)) {
            log.info("订单已支付，跳过重复回调 orderNo={}", outTradeNo);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        VipOrder orderUpdate = new VipOrder();
        orderUpdate.setId(order.getId());
        orderUpdate.setPayStatus(VipOrder.STATUS_PAID);
        orderUpdate.setTradeNo(tradeNo);
        orderUpdate.setPayTime(now);
        iVipOrderService.updateById(orderUpdate);

        activateVip(order.getUserId(), order.getPlanDays(), now);
        referralCommissionTriggerService.triggerOnVipPaySuccess(order);
        log.info("VIP 开通成功 userId={} orderNo={} planDays={}", order.getUserId(), outTradeNo, order.getPlanDays());
    }

    /**
     * 开通或续费 VIP：叠加到期时间，重置当日 OCR/AI 次数（文档 23.3，见 MemberRulesSupport）
     */
    void activateVip(Long userId, int planDays, LocalDateTime now) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            log.error("VIP 开通失败，用户不存在 userId={}", userId);
            return;
        }

        LocalDateTime newExpire = memberRulesSupport.calculateVipExpireAt(user, planDays, now);
        ActivationQuotaReset quotaReset = memberRulesSupport.buildActivationQuotaReset(LocalDate.now());

        iUserService.update(new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userId)
                .set(UserEntity::getIsVip, MemberRulesSupport.VIP_FLAG)
                .set(UserEntity::getUserType, MemberRulesSupport.USER_TYPE_VIP)
                .set(UserEntity::getVipExpireAt, newExpire)
                .set(UserEntity::getOcrUsedToday, quotaReset.getOcrUsedToday())
                .set(UserEntity::getAiUsedToday, quotaReset.getAiUsedToday())
                .set(UserEntity::getOcrQuotaDate, quotaReset.getOcrQuotaDate())
                .set(UserEntity::getAiQuotaDate, quotaReset.getAiQuotaDate()));
    }
}
