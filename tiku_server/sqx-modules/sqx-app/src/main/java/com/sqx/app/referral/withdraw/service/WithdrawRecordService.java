package com.sqx.app.referral.withdraw.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.referral.withdraw.domain.bo.*;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawApplyResultVO;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawRulesVO;
import com.sqx.app.referral.withdraw.domain.vo.WithdrawRecordVo;

/**
 * 鎻愮幇璁板綍 鏈嶅姟绫?
 *
 * @author 瑗垮畨鐪侀挶鍏勭綉缁滅鎶€鏈夐檺鍏徃
 * @since 2025-08-05 10:10:32
 */
public interface WithdrawRecordService {

    void saveInfo(WithdrawRecordSaveBo withdrawRecordSaveBo);

    /**
     * 用户端提现申请（POST /app/withdrawRecord），入库待审核并冻结钱包余额
     */
    AppWithdrawApplyResultVO applyWithdraw(Long userId, AppWithdrawApplyBO applyBO);

    /**
     * 用户端提现规则（最低/最高/手续费）
     */
    AppWithdrawRulesVO getWithdrawRules();

    IPage<WithdrawRecordVo> pageInfo(WithdrawRecordPageBo withdrawRecordPageBo);

    WithdrawRecordVo info(WithdrawRecordInfoBo withdrawRecordInfoBo);

    void updateInfo(WithdrawRecordUpdateBo withdrawRecordUpdateBo);

    void delete(WithdrawRecordInfoBo withdrawRecordInfoBo);

    void confirm(WithdrawRecordConfirmBo withdrawRecordConfirmBo);
}
