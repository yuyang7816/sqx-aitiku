package com.sqx.app.wallet.transaction.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户钱包交易记录表 Mapper 接口
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
public interface UserWalletTransactionMapper extends BaseMapper<UserWalletTransaction> {

    /**
     * 累计有效收入（已到账 + 待结算）
     */
    BigDecimal sumTotalIncome(@Param("userId") Long userId);

    /**
     * 待结算收入合计
     */
    BigDecimal sumPendingIncome(@Param("userId") Long userId);
}
