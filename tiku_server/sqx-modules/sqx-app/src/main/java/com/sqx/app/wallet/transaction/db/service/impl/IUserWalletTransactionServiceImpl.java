package com.sqx.app.wallet.transaction.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.wallet.transaction.db.mapper.UserWalletTransactionMapper;
import com.sqx.app.wallet.transaction.db.service.IUserWalletTransactionService;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户钱包交易记录表 服务实现类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
@Service
public class IUserWalletTransactionServiceImpl extends ServiceImpl<UserWalletTransactionMapper, UserWalletTransaction> implements IUserWalletTransactionService {

}
