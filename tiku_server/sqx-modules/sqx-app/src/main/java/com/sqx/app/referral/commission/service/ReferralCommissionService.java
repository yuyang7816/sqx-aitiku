package com.sqx.app.referral.commission.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionInfoBo;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionPageBo;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionSaveBo;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionUpdateBo;
import com.sqx.app.referral.commission.domain.vo.ReferralCommissionVo;

/**
 * 佣金信息 服务类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
public interface ReferralCommissionService {

    void saveInfo(ReferralCommissionSaveBo referralCommissionSaveBo);

    IPage<ReferralCommissionVo> pageInfo(ReferralCommissionPageBo referralCommissionPageBo);

    ReferralCommissionVo info(ReferralCommissionInfoBo referralCommissionInfoBo);

    void updateInfo(ReferralCommissionUpdateBo referralCommissionUpdateBo);

    void delete(ReferralCommissionInfoBo referralCommissionInfoBo);
}
