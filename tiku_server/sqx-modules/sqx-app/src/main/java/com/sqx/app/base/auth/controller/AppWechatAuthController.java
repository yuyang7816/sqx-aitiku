package com.sqx.app.base.auth.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.domain.bo.AppWechatBindPhoneBO;
import com.sqx.app.base.auth.domain.bo.AppWechatLoginBO;
import com.sqx.app.base.auth.domain.vo.AppWechatLoginVO;
import com.sqx.app.base.auth.service.AppAuthService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端微信 OAuth 登录（与 /app/auth/sms 路径风格一致）
 */
@Tag(name = "用户端-认证", description = "文档 21.1：用户端短信/微信登录与验证码（/app/auth/*、/app/verifyCode）")
@RestController
@RequestMapping("/app/auth")
public class AppWechatAuthController {

    @Autowired
    private AppAuthService appAuthService;

    @Operation(summary = "微信 OAuth 登录", description = "uni.login code 换 openId，校验 agreedProtocol，自动注册/登录并签发 JWT；始终返回 profile.ready。错误码：50001/350012/350002/350019，见 docs/login-rules-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppWechatLoginVO.class)))
    @PostMapping("wechat")
    public Result<AppWechatLoginVO> wechatLogin(@RequestBody @Validated AppWechatLoginBO appWechatLoginBO) {
        return appAuthService.wechatLogin(appWechatLoginBO);
    }

    @Operation(summary = "微信公众号 OAuth 登录", description = "H5 微信内置浏览器 sns/oauth2 code 换 openId，校验 agreedProtocol，自动注册/登录并签发 JWT")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppWechatLoginVO.class)))
    @PostMapping("mp")
    public Result<AppWechatLoginVO> mpOAuthLogin(@RequestBody @Validated AppWechatLoginBO appWechatLoginBO) {
        return appAuthService.mpOAuthLogin(appWechatLoginBO);
    }

    @Operation(summary = "微信小程序绑定手机号", description = "登录后使用 button getPhoneNumber 返回的 code，服务端调用微信 getuserphonenumber 官方 API；同手机号跨端合并为同一用户")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppWechatLoginVO.class)))
    @Login
    @PostMapping("wechat/bindPhone")
    public Result<AppWechatLoginVO> bindWechatPhone(@RequestAttribute("userId") Long userId,
                                                    @RequestBody @Validated AppWechatBindPhoneBO bindPhoneBO) {
        return appAuthService.bindWechatPhone(userId, bindPhoneBO);
    }
}
