package com.sqx.app.referral.invitecode.service;

import com.sqx.app.referral.invitecode.domain.vo.AppReferralInviteCodeVO;

/**
 * 推广邀请码与带参小程序码
 */
public interface ReferralInviteCodeService {

    /**
     * 获取当前用户唯一邀请码及带 scene 的小程序码 URL
     */
    AppReferralInviteCodeVO getInviteCode(Long userId);
}
