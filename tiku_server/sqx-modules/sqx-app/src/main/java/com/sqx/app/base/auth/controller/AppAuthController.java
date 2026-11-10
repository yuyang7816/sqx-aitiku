package com.sqx.app.base.auth.controller;

import com.sqx.app.base.auth.domain.bo.*;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.domain.vo.WxLoginVo;
import com.sqx.app.base.auth.service.AppAuthService;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.utils.wx.WxUserPhone;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author chengming
 * @since 2025/4/22
 */
@Tag(name = "用户端-登录", description = "用户端-登录")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/auth")
public class AppAuthController {

    @Autowired
    private AppAuthService appAuthService;

    @Operation(summary = "手机号密码登录")
    @PostMapping("phonePasswordLogin")
    public Result<LoginVO> phonePasswordLogin(@RequestBody PhonePasswordLoginBO phonePasswordLoginBO) {
        return appAuthService.phonePasswordLogin(phonePasswordLoginBO);
    }

    @Operation(summary = "手机号验证码登录（如果未注册会先注册）")
    @PostMapping("phoneCodeLogin")
    public Result<LoginVO> phoneCodeLogin(@RequestBody PhoneCodeLoginBO phoneCodeLoginBO) {
        return appAuthService.phoneCodeLogin(phoneCodeLoginBO);
    }

    @Operation(summary = "手机号注册")
    @PostMapping("phoneRegister")
    public Result<LoginVO> phoneRegister(@RequestBody PhoneRegisterBO phoneRegisterBO) {
        return appAuthService.phoneRegister(phoneRegisterBO);
    }

    @Operation(summary = "忘记密码")
    @PostMapping("resetPassword")
    public Result<Void> resetPassword(@RequestBody ResetPasswordBO resetPasswordBO) {
        return appAuthService.resetPassword(resetPasswordBO);
    }

    @Operation(summary = "微信小程序登陆")
    @GetMapping("wxLogin")
    public Result<WxLoginVo> wxLogin(String code){
        return appAuthService.wxLogin(code);
    }

    @Operation(summary = "微信小程序注册")
    @PostMapping("wxJsRegister")
    public Result<LoginVO> wxJsRegister(@RequestBody WxJsRegisterBo wxJsRegisterBo) {
        return appAuthService.wxJsRegister(wxJsRegisterBo);
    }

    @Operation(summary = "解密手机号")
    @PostMapping("decryptPhone")
    public Result<Object> decryptPhone(@RequestBody DecryptPhoneBo decryptPhoneBo) {
        return appAuthService.decryptPhone(decryptPhoneBo);
    }

    @Operation(summary = "微信获取用户手机号")
    @GetMapping("getWxUserPhoneNumber")
    public Result<WxUserPhone> getWxUserPhoneNumber(@RequestParam(value = "code") String code,
                                                    @RequestParam(value = "openId", required = false) String openId) {
        WxUserPhone wxUserPhone = appAuthService.getWxUserPhoneNumber(code, openId);
        return Result.success(wxUserPhone);
    }
}