package com.sqx.app.referral.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.service.ReferralCommissionTriggerService;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.domain.vo.ReferralListVo;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.referral.user.domain.bo.ReferralUserCommissionInfoBo;
import com.sqx.app.referral.user.domain.bo.ReferralUserCommissionUpdateBo;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;
import com.sqx.app.referral.user.domain.vo.AppUserCommissionSummaryVO;
import com.sqx.app.referral.user.domain.vo.ReferralDataVo;
import com.sqx.app.referral.user.domain.vo.ReferralLawyerVo;
import com.sqx.app.referral.user.domain.vo.ReferralUserCommissionVo;
import com.sqx.app.referral.user.service.ReferralUserCommissionService;
import com.sqx.app.utils.DateUtil;
import com.sqx.app.utils.model.StartAndEndTime;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.shiro.utils.UserContextHolder;
import com.sqx.system.db.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户佣金 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:53
 */
@Slf4j
@Service
public class ReferralUserCommissionServiceImpl implements ReferralUserCommissionService {

    @Resource
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Resource
    private ISysConfigService iSysConfigService;
    @Resource
    private IUserService iUserService;
    @Resource
    private IReferralCommissionService iReferralCommissionService;
    @Resource
    private ReferralCommissionTriggerService referralCommissionTriggerService;
    @Resource
    private IVipOrderService iVipOrderService;

    @Override
    public ReferralUserCommissionVo info(Long userId) {
        ReferralUserCommission referralUserCommission = iReferralUserCommissionService.getOne(new LambdaQueryWrapper<ReferralUserCommission>().eq(ReferralUserCommission::getUserId, userId));
        if (Objects.isNull(referralUserCommission)) {
            throw new BizException("信息不存在");
        }

        ReferralUserCommissionVo vo = new ReferralUserCommissionVo();
        BeanUtils.copyProperties(referralUserCommission, vo);
        return vo;
    }

    @Override
    public void updateInfo(ReferralUserCommissionUpdateBo referralUserCommissionUpdateBo) {
        ReferralUserCommission referralUserCommission = new ReferralUserCommission();
        BeanUtils.copyProperties(referralUserCommissionUpdateBo, referralUserCommission);

        iReferralUserCommissionService.updateById(referralUserCommission);
    }

    @Override
    public void delete(ReferralUserCommissionInfoBo referralUserCommissionInfoBo) {
        iReferralUserCommissionService.removeById(referralUserCommissionInfoBo.getId());
    }

    @Override
    public void saveUserCommission(Long orderId) {
        if (orderId == null) {
            log.info("订单 ID 为空，不计算佣金");
            return;
        }
        VipOrder order = iVipOrderService.getById(orderId);
        if (order == null) {
            log.info("会员订单不存在, 不计算佣金 orderId={}", orderId);
            return;
        }
        referralCommissionTriggerService.triggerOnVipPaySuccess(order);
    }

    @Override
    public AppUserCommissionSummaryVO getSummary(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        long inviteCount = 0L;
        if (StringUtils.isNotBlank(user.getInvitationCode())) {
            QueryWrapper<UserEntity> inviteQuery = new QueryWrapper<>();
            inviteQuery.lambda().eq(UserEntity::getSuperiorInvitationCode, user.getInvitationCode());
            inviteCount = iUserService.count(inviteQuery);
        }

        BigDecimal totalCommission = normalizeAmount(iReferralCommissionService.sumUserCommission(userId));

        StartAndEndTime todayRange = DateUtil.today();
        BigDecimal todayCommission = normalizeAmount(
                iReferralCommissionService.sumAmount(userId, todayRange.getStartTime(), todayRange.getEndTime())
        );

        AppUserCommissionSummaryVO vo = new AppUserCommissionSummaryVO();
        vo.setTotalCommission(totalCommission);
        vo.setTodayCommission(todayCommission);
        vo.setInviteCount(inviteCount);
        return vo;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    @Override
    public ReferralDataVo referralData(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        // 邀请人数
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getSuperiorInvitationCode, user.getInvitationCode());
        long count = iUserService.count(queryWrapper);

        // 佣金
        BigDecimal commission = iReferralCommissionService.sumUserCommission(userId);

        ReferralDataVo vo = new ReferralDataVo();
        vo.setNum(count);
        vo.setCommission(commission);
        return vo;
    }

    @Override
    public IPage<UserEntity> referralUserList(Long userId) {
        Page<UserEntity> page = new Page<>(1, 10);

        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getSuperiorInvitationCode, user.getInvitationCode());
        IPage<UserEntity> userEntityPage = iUserService.page(page, queryWrapper);

        return userEntityPage;
    }

    @Override
    public IPage<ReferralListVo> referralList(Long userId) {
        Page<ReferralCommission> page = new Page<>(1, 10);

        IPage<ReferralListVo> voIPage = iReferralCommissionService.referralList(page, userId);

        return voIPage;
    }

    @Override
    public ReferralLawyerVo lawyer() {
        ReferralCommission referralCommission = iReferralCommissionService.getByUserId(UserContextHolder.getAppUserId());
        if (Objects.isNull(referralCommission)) {
            log.error("用户[{}]资产不存在", UserContextHolder.getAppUserId());
            throw new BizException("用户资产不存在");
        }

        // 昨日收益
        StartAndEndTime startAndEndTime = DateUtil.getStartAndEndTime(LocalDate.now().plusDays(1));
        BigDecimal yesterdayEarn = iReferralCommissionService.sumAmount(UserContextHolder.getAppUserId(), startAndEndTime.getStartTime(), startAndEndTime.getEndTime());

        ReferralLawyerVo vo = new ReferralLawyerVo();
        vo.setAmount(referralCommission.getAmount());
        vo.setYesterdayEarn(yesterdayEarn);
        return vo;
    }
}
