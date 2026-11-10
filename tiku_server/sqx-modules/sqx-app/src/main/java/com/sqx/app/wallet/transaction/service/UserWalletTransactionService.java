package com.sqx.app.wallet.transaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionInfoBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionPageBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionSaveBo;
import com.sqx.app.wallet.transaction.domain.bo.UserWalletTransactionUpdateBo;
import com.sqx.app.wallet.transaction.domain.vo.AppUserWalletTransactionDetailVO;
import com.sqx.app.wallet.transaction.domain.vo.UserWalletTransactionVo;

/**
 * 用户钱包交易记录表 服务类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:12:03
 */
public interface UserWalletTransactionService {

    /**
     * 收益流水详情：累计/待结算汇总 + 分页流水列表
     */
    AppUserWalletTransactionDetailVO getDetail(Long userId, Long page, Long limit);

    void saveInfo(UserWalletTransactionSaveBo userWalletTransactionSaveBo);

    IPage<UserWalletTransactionVo> pageInfo(UserWalletTransactionPageBo userWalletTransactionPageBo);

    UserWalletTransactionVo info(UserWalletTransactionInfoBo userWalletTransactionInfoBo);

    void updateInfo(UserWalletTransactionUpdateBo userWalletTransactionUpdateBo);

    void delete(UserWalletTransactionInfoBo userWalletTransactionInfoBo);
}
