package com.sqx.app.wallet.wallet.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户钱包主表 Mapper 接口
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:09:58
 */
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    void incr(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    void decr(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
