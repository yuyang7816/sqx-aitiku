package com.sqx.app.referral.invitecode.service.impl;

import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.invitecode.domain.vo.AppReferralInviteCodeVO;
import com.sqx.app.referral.invitecode.service.ReferralInviteCodeService;
import com.sqx.app.referral.invitecode.support.InviteCodeSupport;
import com.sqx.app.referral.invitecode.support.ReferralMiniProgramQrSupport;
import com.sqx.app.referral.invitecode.support.ReferralPosterConfigSupport;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 推广邀请码与带参小程序码
 */
@Service
public class ReferralInviteCodeServiceImpl implements ReferralInviteCodeService {

    @Resource
    private IUserService iUserService;
    @Resource
    private InviteCodeSupport inviteCodeSupport;
    @Resource
    private ReferralMiniProgramQrSupport referralMiniProgramQrSupport;
    @Resource
    private ReferralPosterConfigSupport referralPosterConfigSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppReferralInviteCodeVO getInviteCode(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        String inviteCode = inviteCodeSupport.ensureUniqueInviteCode(user);
        String qrCodeUrl = referralMiniProgramQrSupport.buildQrCodeUrl(inviteCode);

        AppReferralInviteCodeVO vo = new AppReferralInviteCodeVO();
        vo.setInviteCode(inviteCode);
        vo.setQrCodeUrl(qrCodeUrl);
        vo.setPosterImageUrl(referralPosterConfigSupport.resolvePosterImageUrl());
        vo.setInviteText(referralPosterConfigSupport.resolveInviteText());
        return vo;
    }
}
