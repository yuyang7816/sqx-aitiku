package com.sqx.app.base.auth.service.impl;

import com.sqx.app.base.auth.domain.bo.AppWechatLoginBO;
import com.sqx.app.base.auth.domain.vo.AppWechatLoginVO;
import com.sqx.app.base.auth.domain.vo.LoginProfileStateVO;
import com.sqx.app.base.auth.support.LoginRulesSupport;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.wx.WxJsLoginRes;
import com.sqx.app.base.utils.wx.WxUtil;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.wallet.wallet.db.service.IUserWalletService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.shiro.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户端微信 OAuth 登录单元测试
 */
@ExtendWith(MockitoExtension.class)
class AppAuthServiceImplWechatLoginTest {

    private static final String CODE = "valid-wx-code";
    private static final String OPEN_ID = "oTestOpenId123456";

    @Mock
    private IUserService iUserService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private WxUtil wxUtil;
    @Mock
    private IUserWalletService iUserWalletService;
    @Mock
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Mock
    private LoginRulesSupport loginRulesSupport;

    @Spy
    @InjectMocks
    private AppAuthServiceImpl appAuthService;

    @Test
    void loginByWechatSession_whenUserExists_shouldReturnTokenAndProfileReady() {
        WxJsLoginRes wxRes = buildWxSession();

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(200L);
        userEntity.setMaOpenId(OPEN_ID);
        userEntity.setUserName("老用户");
        userEntity.setStatus(1);
        userEntity.setProfileReady(1);

        LoginProfileStateVO profileState = new LoginProfileStateVO();
        profileState.setReady(true);

        when(iUserService.getByOpenId(OPEN_ID)).thenReturn(userEntity);
        when(jwtUtil.generateToken(200L)).thenReturn("jwt-wechat-token");
        when(jwtUtil.getExpire()).thenReturn(604800L);
        when(loginRulesSupport.buildProfileState(userEntity)).thenReturn(profileState);

        Result<AppWechatLoginVO> result = appAuthService.loginByWechatSession(wxRes);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt-wechat-token", result.getData().getToken());
        assertEquals("老用户", result.getData().getUser().getUserName());
        assertFalse(result.getData().getFirstLogin());
        assertNotNull(result.getData().getProfile());
        assertTrue(result.getData().getProfile().getReady());
    }

    @Test
    void loginByWechatSession_whenNewUser_shouldRegisterAndReturnProfileReadyFalse() {
        WxJsLoginRes wxRes = buildWxSession();

        when(iUserService.getByOpenId(OPEN_ID)).thenReturn(null);
        doAnswer(invocation -> {
            UserEntity saved = invocation.getArgument(0);
            saved.setUserId(300L);
            return true;
        }).when(iUserService).save(any(UserEntity.class));
        when(jwtUtil.generateToken(300L)).thenReturn("jwt-new-user-token");
        when(jwtUtil.getExpire()).thenReturn(604800L);
        LoginProfileStateVO profileState = new LoginProfileStateVO();
        profileState.setReady(false);
        when(loginRulesSupport.buildProfileState(any(UserEntity.class))).thenReturn(profileState);

        Result<AppWechatLoginVO> result = appAuthService.loginByWechatSession(wxRes);

        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt-new-user-token", result.getData().getToken());
        assertTrue(result.getData().getFirstLogin());
        assertNotNull(result.getData().getProfile());
        assertFalse(result.getData().getProfile().getReady());
        verify(iUserWalletService).save(any());
        verify(iReferralUserCommissionService).save(any());
    }

    @Test
    void wechatLogin_whenConfigMissing_shouldThrowWechatNotConfigured() {
        AppWechatLoginBO bo = new AppWechatLoginBO();
        bo.setCode(CODE);
        bo.setAgreedProtocol(true);

        doThrow(new BizException(AppError.WECHAT_NOT_CONFIGURED))
                .when(appAuthService).fetchWechatSession(CODE, "ma");

        BizException ex = assertThrows(BizException.class, () -> appAuthService.wechatLogin(bo));
        assertEquals(AppError.WECHAT_NOT_CONFIGURED, ex.getErrorCode());
    }

    @Test
    void wechatLogin_whenCodeInvalid_shouldThrowWechatCodeInvalid() {
        AppWechatLoginBO bo = new AppWechatLoginBO();
        bo.setCode("bad-code");
        bo.setAgreedProtocol(true);

        doThrow(new BizException(AppError.WECHAT_CODE_INVALID))
                .when(appAuthService).fetchWechatSession("bad-code", "ma");

        BizException ex = assertThrows(BizException.class, () -> appAuthService.wechatLogin(bo));
        assertEquals(AppError.WECHAT_CODE_INVALID, ex.getErrorCode());
    }

    @Test
    void fetchWechatSession_whenMockDisabledAndDevtoolsCode_shouldThrowSimulatorError() {
        org.springframework.test.util.ReflectionTestUtils.setField(appAuthService, "wechatMock", false);

        BizException ex = assertThrows(BizException.class,
                () -> appAuthService.fetchWechatSession("the code is a mock one", "ma"));
        assertEquals(AppError.WECHAT_SIMULATOR_CODE, ex.getErrorCode());
    }

    @Test
    void fetchWechatSession_whenMockEnabledAndMockCode_shouldReturnMockOpenId() {
        org.springframework.test.util.ReflectionTestUtils.setField(appAuthService, "wechatMock", true);
        org.springframework.test.util.ReflectionTestUtils.setField(appAuthService, "wechatMockOpenid", "oDevMockWxLoginOpenId");

        doReturn("wxTestAppId").when(appAuthService).requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MA_APPID);
        doReturn("test-secret").when(appAuthService).requireWechatConfig(SysConfigConstant.Sys.SYS_USER_MA_SECRET);

        WxJsLoginRes res = appAuthService.fetchWechatSession("the code is a mock one", "ma");

        assertEquals("oDevMockWxLoginOpenId", res.getOpenid());
        assertNotNull(res.getSessionKey());
    }

    private WxJsLoginRes buildWxSession() {
        WxJsLoginRes wxRes = new WxJsLoginRes();
        wxRes.setOpenid(OPEN_ID);
        wxRes.setSessionKey("session-key");
        return wxRes;
    }
}
