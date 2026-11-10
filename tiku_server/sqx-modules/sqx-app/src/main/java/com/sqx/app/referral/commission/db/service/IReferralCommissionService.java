package com.sqx.app.referral.commission.db.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.domain.vo.ReferralListVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 佣金信息 服务类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
public interface IReferralCommissionService extends IService<ReferralCommission> {

    BigDecimal sumUserCommission(Long userId);

    IPage<ReferralListVo> referralList(Page<ReferralCommission> page, Long userId);

    ReferralCommission getByUserId(Long userId);

    /**
     * 统计金额
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    BigDecimal sumAmount(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    void saveRefuseWithdraw(Long userId, BigDecimal amount);

    void saveWithdraw(Long userId, BigDecimal amount);
}
