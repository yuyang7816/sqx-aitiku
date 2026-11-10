package com.sqx.app.referral.commission.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.referral.commission.db.mapper.ReferralCommissionMapper;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.domain.vo.ReferralListVo;
import com.sqx.app.referral.commission.enums.Direction;
import com.sqx.app.referral.commission.enums.ReferralCommissionType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 佣金信息 服务实现类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
@Service
public class IReferralCommissionServiceImpl extends ServiceImpl<ReferralCommissionMapper, ReferralCommission> implements IReferralCommissionService {

    @Resource
    private ReferralCommissionMapper referralCommissionMapper;

    @Override
    public BigDecimal sumUserCommission(Long userId) {
        return referralCommissionMapper.sumUserCommission(userId);
    }

    @Override
    public IPage<ReferralListVo> referralList(Page<ReferralCommission> page, Long userId) {
        return referralCommissionMapper.referralList(page, userId);
    }

    @Override
    public ReferralCommission getByUserId(Long userId) {
        LambdaQueryWrapper<ReferralCommission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReferralCommission::getUserId, userId);

        return this.getOne(queryWrapper);
    }


    @Override
    public BigDecimal sumAmount(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return this.baseMapper.sumAmount(userId, startTime, endTime);
    }

    @Override
    public void saveRefuseWithdraw(Long userId, BigDecimal amount) {
        ReferralCommission referralCommission = new ReferralCommission();
        referralCommission.setUserId(userId);
        referralCommission.setAmount(amount);
        referralCommission.setType(ReferralCommissionType.WITHDRAW.getValue());
        referralCommission.setDirection(Direction.INCR.getValue());
        referralCommission.setTitle("提现失败");
        referralCommission.setDescription("提现失败: " + amount.toPlainString());
        this.save(referralCommission);
    }

    @Override
    public void saveWithdraw(Long userId, BigDecimal amount) {
        ReferralCommission referralCommission = new ReferralCommission();
        referralCommission.setUserId(userId);
        referralCommission.setAmount(amount);
        referralCommission.setType(ReferralCommissionType.WITHDRAW.getValue());
        referralCommission.setDirection(Direction.DECR.getValue());
        referralCommission.setTitle("提现");
        referralCommission.setDescription("发起提现: " + amount.toPlainString());
        this.save(referralCommission);
    }
}
