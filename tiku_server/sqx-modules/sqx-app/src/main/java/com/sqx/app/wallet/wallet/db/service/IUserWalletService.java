package com.sqx.app.wallet.wallet.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.wallet.wallet.domain.entity.UserWallet;

import java.math.BigDecimal;

/**
 * <p>
 * 用户钱包主表 服务类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:09:58
 */
public interface IUserWalletService extends IService<UserWallet> {

    void incr(Long userId, BigDecimal amount);

    void decr(Long userId, BigDecimal amount);
}
