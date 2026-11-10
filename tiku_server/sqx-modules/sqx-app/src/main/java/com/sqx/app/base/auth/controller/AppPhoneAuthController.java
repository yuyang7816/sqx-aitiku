package com.sqx.app.base.auth.controller;

import com.sqx.app.base.auth.domain.bo.PhonePasswordLoginBO;
import com.sqx.app.base.auth.domain.bo.PhoneRegisterBO;
import com.sqx.app.base.auth.domain.bo.ResetPasswordBO;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.service.AppAuthService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端手机号注册 / 密码登录 / 重置密码（与 /app/auth/sms 路径风格一致）
 */
@Tag(name = "用户端-认证", description = "文档 21.1：用户端短信/微信登录与验证码（/app/auth/*、/app/verifyCode）")
@RestController
@RequestMapping("/app/auth")
public class AppPhoneAuthController {

    @Autowired
    private AppAuthService appAuthService;

    @Operation(summary = "手机号密码登录")
    @PostMapping("phonePasswordLogin")
    public Result<LoginVO> phonePasswordLogin(@RequestBody @Validated PhonePasswordLoginBO phonePasswordLoginBO) {
        return appAuthService.phonePasswordLogin(phonePasswordLoginBO);
    }

    @Operation(summary = "手机号注册")
    @PostMapping("phoneRegister")
    public Result<LoginVO> phoneRegister(@RequestBody @Validated PhoneRegisterBO phoneRegisterBO) {
        return appAuthService.phoneRegister(phoneRegisterBO);
    }

    @Operation(summary = "忘记密码")
    @PostMapping("resetPassword")
    public Result<Void> resetPassword(@RequestBody @Validated ResetPasswordBO resetPasswordBO) {
        return appAuthService.resetPassword(resetPasswordBO);
    }
}
