package com.sqx.app.referral.invitecode.service.impl;

import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.invitecode.domain.vo.AppReferralInviteCodeVO;
import com.sqx.app.referral.invitecode.support.InviteCodeSupport;
import com.sqx.app.referral.invitecode.support.ReferralMiniProgramQrSupport;
import com.sqx.app.referral.invitecode.support.ReferralPosterConfigSupport;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/referral/inviteCode 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ReferralInviteCodeServiceImplTest {

    @Mock
    private IUserService iUserService;
    @Mock
    private InviteCodeSupport inviteCodeSupport;
    @Mock
    private ReferralMiniProgramQrSupport referralMiniProgramQrSupport;
    @Mock
    private ReferralPosterConfigSupport referralPosterConfigSupport;

    @InjectMocks
    private ReferralInviteCodeServiceImpl referralInviteCodeService;

    @Test
    void getInviteCode_whenUserNotFound_shouldThrow() {
        when(iUserService.getById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> referralInviteCodeService.getInviteCode(999L));
    }

    @Test
    void getInviteCode_whenUserExists_shouldReturnInviteCodeAndQrUrl() {
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setInvitationCode("INV8000");
        when(iUserService.getById(1L)).thenReturn(user);
        when(inviteCodeSupport.ensureUniqueInviteCode(user)).thenReturn("INV8000");
        when(referralMiniProgramQrSupport.buildQrCodeUrl("INV8000"))
                .thenReturn("http://localhost:9135/sqx_fast/upload/referral/qr/INV8000.png");
        when(referralPosterConfigSupport.resolvePosterImageUrl())
                .thenReturn("http://localhost:9135/sqx_fast/upload/poster.png");
        when(referralPosterConfigSupport.resolveInviteText())
                .thenReturn("扫码体验 AI 智能提分");

        AppReferralInviteCodeVO result = referralInviteCodeService.getInviteCode(1L);

        assertEquals("INV8000", result.getInviteCode());
        assertEquals("http://localhost:9135/sqx_fast/upload/referral/qr/INV8000.png", result.getQrCodeUrl());
        assertEquals("http://localhost:9135/sqx_fast/upload/poster.png", result.getPosterImageUrl());
        assertEquals("扫码体验 AI 智能提分", result.getInviteText());
        verify(inviteCodeSupport).ensureUniqueInviteCode(user);
        verify(referralMiniProgramQrSupport).buildQrCodeUrl("INV8000");
    }

    @Test
    void getInviteCode_whenCodeGenerated_shouldUseSameCodeForQr() {
        UserEntity user = new UserEntity();
        user.setUserId(2L);
        when(iUserService.getById(2L)).thenReturn(user);
        when(inviteCodeSupport.ensureUniqueInviteCode(user)).thenReturn("ABCDEF");
        when(referralMiniProgramQrSupport.buildQrCodeUrl("ABCDEF"))
                .thenReturn("http://localhost/upload/referral/qr/ABCDEF.png");
        when(referralPosterConfigSupport.resolvePosterImageUrl()).thenReturn("");
        when(referralPosterConfigSupport.resolveInviteText()).thenReturn("");

        AppReferralInviteCodeVO result = referralInviteCodeService.getInviteCode(2L);

        assertEquals("ABCDEF", result.getInviteCode());
        assertEquals("http://localhost/upload/referral/qr/ABCDEF.png", result.getQrCodeUrl());
    }
}
