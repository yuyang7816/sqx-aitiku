package com.sqx.app.referral.user.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.user.db.mapper.ReferralUserCommissionMapper;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * <p>
 * 用户佣金 服务实现类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:53
 */
@Slf4j
@Service
public class IReferralUserCommissionServiceImpl extends ServiceImpl<ReferralUserCommissionMapper, ReferralUserCommission> implements IReferralUserCommissionService {

    @Resource
    private ReferralUserCommissionMapper referralUserCommissionMapper;
    @Resource
    private IReferralCommissionService iReferralCommissionService;
    @Resource
    private IUserService iUserService;

    @Override
    public void incr(Long userId, BigDecimal commission) {
        referralUserCommissionMapper.incr(userId, commission);
    }

    @Override
    public ReferralUserCommission getByUserId(Long userId) {
        QueryWrapper<ReferralUserCommission> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReferralUserCommission::getUserId, userId);

        return this.getOne(queryWrapper);
    }

    @Override
    public void decr(Long userId, BigDecimal amount) {
        referralUserCommissionMapper.decr(userId, amount);
    }

    @Override
    public void incrInvite(Long orderId, Long userId, BigDecimal commissionRate, BigDecimal amount) {
        // 计算佣金
        BigDecimal commission = amount.multiply(commissionRate);

        this.incr(userId, commission);

        // 保存用户佣金记录
        ReferralCommission referralCommission = new ReferralCommission();
        referralCommission.setUserId(userId);
        referralCommission.setOrderId(orderId);
        referralCommission.setRate(commissionRate);
        referralCommission.setAmount(commission);
        referralCommission.setType(1);
        iReferralCommissionService.save(referralCommission);
    }

    @Override
    public void saveOrder(Long userId, Long orderId, BigDecimal amount) {
//        OmsOrder order = iOmsOrderService.getById(orderId);
//        if (Objects.isNull(order)) {
//            log.error("订单不存在: [{}]", orderId);
//            throw new BizException("订单不存在");
//        }
//
//        UserEntity user = iUserService.getById(order.getUserId());
//        if (Objects.isNull(user)) {
//            log.error("用户不存在: [{}]", order.getUserId());
//            throw new BizException("用户不存在");
//        }
//
//        UserEntity inviteUser = iUserService.getByInvitationCode(user.getSuperiorInvitationCode());
//        if (Objects.isNull(inviteUser)) {
//            log.error("上级用户不存在: [{}]", order.getUserId());
//            throw new BizException("上级用户不存在");
//        }
//
//        // 佣金
//        BigDecimal commission = amount.multiply(inviteUser.getCommissionRate());
//        this.incrInvite(orderId, inviteUser.getUserId(), inviteUser.getCommissionRate(), amount);
//
//        // 律师收益
//        BigDecimal earn = amount.subtract(commission);
//
//        // 律师收益到账
//        this.incr(userId, earn);
//
//        // 记录律师收益明细
//        ReferralCommission referralCommission = new ReferralCommission();
//        referralCommission.setUserId(userId);
//        referralCommission.setOrderId(orderId);
//        referralCommission.setType(2);
//        referralCommission.setAmount(earn);
//        iReferralCommissionService.save(referralCommission);
    }
}
