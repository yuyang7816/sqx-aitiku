package com.sqx.app.referral.user.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户佣金 Mapper 接口
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:53
 */
public interface ReferralUserCommissionMapper extends BaseMapper<ReferralUserCommission> {

    void incr(@Param("userId") Long userId, @Param("commission") BigDecimal commission);

    void decr(@Param("userId")Long userId, @Param("amount") BigDecimal amount);
}
