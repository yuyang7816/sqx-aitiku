package com.sqx.app.referral.commission.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.domain.vo.ReferralListVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 佣金信息 Mapper 接口
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
public interface ReferralCommissionMapper extends BaseMapper<ReferralCommission> {

    BigDecimal sumUserCommission(@Param("userId") Long userId);

    IPage<ReferralListVo> referralList(Page<ReferralCommission> page, @Param("userId") Long userId);

    BigDecimal sumAmount(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /** 指定下级用户贡献的分佣合计（通过订单关联买家） */
    BigDecimal sumMemberCommission(@Param("inviterUserId") Long inviterUserId, @Param("memberUserId") Long memberUserId);
}
