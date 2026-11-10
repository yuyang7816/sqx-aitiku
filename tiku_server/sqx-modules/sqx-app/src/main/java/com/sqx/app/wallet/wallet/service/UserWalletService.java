package com.sqx.app.wallet.wallet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.wallet.wallet.domain.bo.*;
import com.sqx.app.wallet.wallet.domain.vo.AppUserWalletBalanceVO;
import com.sqx.app.wallet.wallet.domain.vo.UserWalletVo;

/**
 * 用户钱包主表 服务类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-13 15:09:58
 */
public interface UserWalletService {

    void saveInfo(UserWalletSaveBo userWalletSaveBo);

    IPage<UserWalletVo> pageInfo(UserWalletPageBo userWalletPageBo);

    UserWalletVo info(UserWalletInfoBo userWalletInfoBo);

    void updateInfo(UserWalletUpdateBo userWalletUpdateBo);

    void delete(UserWalletInfoBo userWalletInfoBo);

    UserWalletVo wallet();

    /**
     * 查询当前用户可提现余额（GET /app/userWallet）
     */
    AppUserWalletBalanceVO getBalance(Long userId);

    void updateAmount(UserWalletUpdateAmountBo userWalletUpdateAmountBo);
}
