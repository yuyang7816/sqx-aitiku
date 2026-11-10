package com.sqx.app.integration.m22;

import com.sqx.app.base.auth.controller.AppSmsAuthController;
import com.sqx.app.base.auth.controller.AppWechatAuthController;
import com.sqx.app.base.auth.domain.bo.AppSmsLoginBO;
import com.sqx.app.base.auth.domain.bo.AppWechatLoginBO;
import com.sqx.app.base.auth.domain.vo.AppWechatLoginVO;
import com.sqx.app.base.auth.domain.vo.LoginProfileStateVO;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.service.AppAuthService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.verifyCode.controller.AppVerifyCodeController;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 模块二十二 P0#2 真实登录 API 集成测试（MockMvc 直调 Controller）
 */
@ExtendWith(MockitoExtension.class)
class M22AuthProductionIntegrationTest {

    private static final String PHONE = "13800138000";
    private static final String CODE = "123456";

    @Mock
    private AppAuthService appAuthService;
    @Mock
    private VerifyCodeService verifyCodeService;

    @InjectMocks
    private AppSmsAuthController smsAuthController;
    @InjectMocks
    private AppWechatAuthController wechatAuthController;
    @InjectMocks
    private AppVerifyCodeController verifyCodeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(smsAuthController, wechatAuthController, verifyCodeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("TC-M22-001 POST /app/verifyCode 发送验证码（匿名）")
    void tcM22001_sendVerifyCode() throws Exception {
        doNothing().when(verifyCodeService).sendAppVerifyCode(PHONE);

        mockMvc.perform(post("/app/verifyCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + PHONE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("TC-M22-002 POST /app/auth/sms 短信登录成功")
    void tcM22002_smsLogin() throws Exception {
        LoginVO loginVO = new LoginVO();
        loginVO.setToken("jwt-m22-token");
        loginVO.setExpire(2592000L);
        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setPhone(PHONE);
        loginVO.setUser(user);

        when(appAuthService.smsLogin(any(AppSmsLoginBO.class))).thenReturn(Result.success(loginVO));

        mockMvc.perform(post("/app/auth/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"" + PHONE + "\",\"code\":\"" + CODE + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("jwt-m22-token"))
                .andExpect(jsonPath("$.data.user.phone").value(PHONE));
    }

    @Test
    @DisplayName("TC-M22-003 POST /app/auth/wechat 微信登录成功（含 firstLogin）")
    void tcM22003_wechatLogin() throws Exception {
        AppWechatLoginVO vo = new AppWechatLoginVO();
        vo.setToken("jwt-wechat-m22");
        vo.setExpire(2592000L);
        vo.setFirstLogin(true);
        UserEntity user = new UserEntity();
        user.setUserId(2L);
        user.setMaOpenId("oM22OpenId");
        vo.setUser(user);
        LoginProfileStateVO profile = new LoginProfileStateVO();
        profile.setReady(false);
        vo.setProfile(profile);

        when(appAuthService.wechatLogin(any(AppWechatLoginBO.class))).thenReturn(Result.success(vo));

        mockMvc.perform(post("/app/auth/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"081validCode\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("jwt-wechat-m22"))
                .andExpect(jsonPath("$.data.firstLogin").value(true))
                .andExpect(jsonPath("$.data.profile.ready").value(false));
    }

    @Test
    @DisplayName("TC-M22-004 POST /app/verifyCode 非法手机号返回 400")
    void tcM22004_invalidPhone() throws Exception {
        mockMvc.perform(post("/app/verifyCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"12345\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(50001));
    }

    @Test
    @DisplayName("TC-M22-005 POST /app/auth/wechat code 为空返回 400")
    void tcM22005_emptyWechatCode() throws Exception {
        mockMvc.perform(post("/app/auth/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(50001));
    }
}
