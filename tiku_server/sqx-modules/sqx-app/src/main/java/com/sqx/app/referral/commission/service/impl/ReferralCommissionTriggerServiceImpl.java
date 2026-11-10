package com.sqx.app.referral.commission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.enums.ReferralCommissionType;
import com.sqx.app.referral.commission.service.ReferralCommissionTriggerService;
import com.sqx.app.referral.commission.support.ReferralCommissionRateSupport;
import com.sqx.app.referral.members.support.ReferralMemberDisplaySupport;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.wallet.transaction.constants.UserWalletTransactionConstant;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import com.sqx.system.db.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 会员支付成功后为上级（一级/二级）计算推广佣金
 */
@Slf4j
@Service
public class ReferralCommissionTriggerServiceImpl implements ReferralCommissionTriggerService {

    /** 用户禁用状态 */
    private static final int USER_STATUS_DISABLED = 2;
    /** 钱包流水：系统调整 */
    private static final int WALLET_TRANSACTION_TYPE_ADJUST = 5;
    /** 钱包流水：活动奖励 */
    private static final int WALLET_BIZ_TYPE_REWARD = 3;

    @Resource
    private IUserService iUserService;
    @Resource
    private ISysConfigService iSysConfigService;
    @Resource
    private IReferralCommissionService iReferralCommissionService;
    @Resource
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Resource
    private IUserWalletService iUserWalletService;
    @Resource
    private IUserWalletTransactionService iUserWalletTransactionService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void triggerOnVipPaySuccess(VipOrder order) {
        if (order == null || order.getId() == null || order.getUserId() == null) {
            log.info("会员订单信息不完整，跳过佣金计算");
            return;
        }

        if (existsCommissionForOrder(order.getId())) {
            log.info("订单已存在佣金记录，跳过重复计算 orderId={}", order.getId());
            return;
        }

        UserEntity buyer = iUserService.getById(order.getUserId());
        if (buyer == null) {
            log.info("下单用户不存在，跳过佣金计算 userId={}", order.getUserId());
            return;
        }

        if (StringUtils.isBlank(buyer.getSuperiorInvitationCode())) {
            log.info("下单用户无上级邀请码，跳过佣金计算 userId={}", order.getUserId());
            return;
        }

        BigDecimal payAmount = normalizeAmount(order.getAmount());
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("订单金额无效，跳过佣金计算 orderId={}", order.getId());
            return;
        }

        UserEntity level1Inviter = findInviter(buyer.getSuperiorInvitationCode());
        grantLevelCommission(level1Inviter, buyer, order, payAmount, 1);

        if (level1Inviter != null && StringUtils.isNotBlank(level1Inviter.getSuperiorInvitationCode())) {
            UserEntity level2Inviter = findInviter(level1Inviter.getSuperiorInvitationCode());
            grantLevelCommission(level2Inviter, buyer, order, payAmount, 2);
        }
    }

    private void grantLevelCommission(UserEntity inviter, UserEntity buyer, VipOrder order,
                                        BigDecimal payAmount, int level) {
        if (inviter == null) {
            log.info("{}级邀请人不存在，跳过佣金计算 orderId={}", level, order.getId());
            return;
        }
        if (Objects.equals(inviter.getStatus(), USER_STATUS_DISABLED)) {
            log.info("{}级邀请人已被禁用，跳过佣金计算 inviterId={}", level, inviter.getUserId());
            return;
        }
        if (Objects.equals(inviter.getUserId(), buyer.getUserId())) {
            log.info("邀请人与下单用户相同，跳过{}级佣金 orderId={}", level, order.getId());
            return;
        }

        BigDecimal commissionRate = level == 1
                ? ReferralCommissionRateSupport.resolveLevel1Rate(inviter, iSysConfigService)
                : ReferralCommissionRateSupport.resolveLevel2Rate(inviter, iSysConfigService);
        BigDecimal commission = payAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        if (commission.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("{}级佣金金额为 0，跳过 orderId={}", level, order.getId());
            return;
        }

        String planName = StringUtils.defaultString(order.getPlanName(), "会员");
        String displayName = ReferralMemberDisplaySupport.resolveDisplayName(buyer);
        String levelLabel = level == 1 ? "一级" : "二级";
        String title = displayName + "开通" + planName + "会员";
        String description = levelLabel + "推广奖励：下级开通" + planName;

        iReferralUserCommissionService.incr(inviter.getUserId(), commission);

        ReferralCommission referralCommission = new ReferralCommission();
        referralCommission.setUserId(inviter.getUserId());
        referralCommission.setOrderId(order.getId());
        referralCommission.setRate(commissionRate);
        referralCommission.setAmount(commission);
        referralCommission.setType(ReferralCommissionType.ORDER.getValue());
        referralCommission.setDirection(UserWalletTransactionConstant.DIRECTION_INCOME);
        referralCommission.setTitle(title);
        referralCommission.setDescription(description);
        iReferralCommissionService.save(referralCommission);

        ensureWallet(inviter.getUserId());
        iUserWalletService.incr(inviter.getUserId(), commission);
        recordWalletTransaction(inviter.getUserId(), commission, order.getId(), title, description);

        log.info("{}级推广佣金已入账 inviterId={} buyerId={} orderId={} commission={}",
                level, inviter.getUserId(), buyer.getUserId(), order.getId(), commission);
    }

    private boolean existsCommissionForOrder(Long orderId) {
        return iReferralCommissionService.count(new LambdaQueryWrapper<ReferralCommission>()
                .eq(ReferralCommission::getOrderId, orderId)
                .eq(ReferralCommission::getType, ReferralCommissionType.ORDER.getValue())) > 0;
    }

    private UserEntity findInviter(String superiorInvitationCode) {
        if (StringUtils.isBlank(superiorInvitationCode)) {
            return null;
        }
        return iUserService.getOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getInvitationCode, superiorInvitationCode.trim())
                .last("LIMIT 1"));
    }

    private void ensureWallet(Long userId) {
        UserWallet wallet = iUserWalletService.getOne(new LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId)
                .last("LIMIT 1"));
        if (wallet != null) {
            return;
        }
        UserWallet newWallet = new UserWallet();
        newWallet.setUserId(userId);
        newWallet.setAmount(BigDecimal.ZERO);
        iUserWalletService.save(newWallet);
    }

    private void recordWalletTransaction(Long userId, BigDecimal amount, Long bizId, String title, String description) {
        UserWalletTransaction transaction = new UserWalletTransaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setTransactionType(WALLET_TRANSACTION_TYPE_ADJUST);
        transaction.setDirection(UserWalletTransactionConstant.DIRECTION_INCOME);
        transaction.setBizType(WALLET_BIZ_TYPE_REWARD);
        transaction.setBizId(bizId);
        transaction.setStatus(UserWalletTransactionConstant.STATUS_COMPLETED);
        transaction.setTitle(title);
        transaction.setDescription(description);
        iUserWalletTransactionService.save(transaction);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
