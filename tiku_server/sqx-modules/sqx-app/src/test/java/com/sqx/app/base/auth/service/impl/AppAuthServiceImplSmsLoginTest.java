package com.sqx.app.base.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.auth.domain.bo.AppSmsLoginBO;
import com.sqx.app.base.auth.domain.vo.LoginProfileStateVO;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.support.LoginRulesSupport;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.shiro.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户端短信验证码登录单元测试
 */
@ExtendWith(MockitoExtension.class)
class AppAuthServiceImplSmsLoginTest {

    private static final String PHONE = "13800138000";
    private static final String CODE = "123456";

    @Mock
    private IUserService iUserService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private VerifyCodeService verifyCodeService;
    @Mock
    private IUserWalletService iUserWalletService;
    @Mock
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Mock
    private LoginRulesSupport loginRulesSupport;

    @InjectMocks
    private AppAuthServiceImpl appAuthService;

    @Test
    void smsLogin_whenCodeValidAndUserExists_shouldReturnTokenAndUser() {
        AppSmsLoginBO bo = new AppSmsLoginBO();
        bo.setPhone(PHONE);
        bo.setCode(CODE);
        bo.setAgreedProtocol(true);

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(100L);
        userEntity.setPhone(PHONE);
        userEntity.setUserName(PHONE);
        userEntity.setStatus(1);
        userEntity.setPassword("secret");

        when(iUserService.getOne(any(QueryWrapper.class))).thenReturn(userEntity);
        when(jwtUtil.generateToken(100L)).thenReturn("jwt-token");
        when(jwtUtil.getExpire()).thenReturn(604800L);
        LoginProfileStateVO profileState = new LoginProfileStateVO();
        profileState.setReady(true);
        when(loginRulesSupport.buildProfileState(userEntity)).thenReturn(profileState);

        Result<LoginVO> result = appAuthService.smsLogin(bo);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt-token", result.getData().getToken());
        assertEquals(PHONE, result.getData().getUser().getPhone());
        assertNull(result.getData().getUser().getPassword());
        assertTrue(result.getData().getProfile().getReady());
        verify(verifyCodeService).verifyForLogin(PHONE, CODE);
        verify(loginRulesSupport).validateAgreementAccepted(true);
    }

    @Test
    void smsLogin_whenAgreementNotAccepted_shouldThrow350019() {
        AppSmsLoginBO bo = new AppSmsLoginBO();
        bo.setPhone(PHONE);
        bo.setCode(CODE);
        bo.setAgreedProtocol(false);

        doThrow(new BizException(AppError.AGREEMENT_NOT_ACCEPTED))
                .when(loginRulesSupport).validateAgreementAccepted(false);

        BizException ex = assertThrows(BizException.class, () -> appAuthService.smsLogin(bo));
        assertEquals(AppError.AGREEMENT_NOT_ACCEPTED, ex.getErrorCode());
    }

    @Test
    void smsLogin_whenCodeWrong_shouldThrowVerifyCodeError() {
        AppSmsLoginBO bo = new AppSmsLoginBO();
        bo.setPhone(PHONE);
        bo.setCode("000000");
        bo.setAgreedProtocol(true);

        doThrow(new BizException(AppError.VERIFY_CODE_ERROR))
                .when(verifyCodeService).verifyForLogin(PHONE, "000000");

        BizException ex = assertThrows(BizException.class, () -> appAuthService.smsLogin(bo));
        assertEquals(AppError.VERIFY_CODE_ERROR, ex.getErrorCode());
    }

    @Test
    void smsLogin_whenCodeExpired_shouldThrowVerifyCodeExpired() {
        AppSmsLoginBO bo = new AppSmsLoginBO();
        bo.setPhone(PHONE);
        bo.setCode(CODE);
        bo.setAgreedProtocol(true);

        doThrow(new BizException(AppError.VERIFY_CODE_EXPIRED))
                .when(verifyCodeService).verifyForLogin(PHONE, CODE);

        BizException ex = assertThrows(BizException.class, () -> appAuthService.smsLogin(bo));
        assertEquals(AppError.VERIFY_CODE_EXPIRED, ex.getErrorCode());
    }

    @Test
    void smsLogin_whenUserBanned_shouldThrowUserBanned() {
        AppSmsLoginBO bo = new AppSmsLoginBO();
        bo.setPhone(PHONE);
        bo.setCode(CODE);
        bo.setAgreedProtocol(true);

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(100L);
        userEntity.setPhone(PHONE);
        userEntity.setStatus(2);

        when(iUserService.getOne(any(QueryWrapper.class))).thenReturn(userEntity);

        BizException ex = assertThrows(BizException.class, () -> appAuthService.smsLogin(bo));
        assertEquals(AppError.USER_BANNED, ex.getErrorCode());
    }
}
