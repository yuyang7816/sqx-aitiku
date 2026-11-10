package com.sqx.app.base.auth.controller;

import com.sqx.app.base.auth.domain.bo.AppSmsLoginBO;
import com.sqx.app.base.auth.domain.vo.LoginVO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端短信登录（与 /app/verifyCode 路径风格一致）
 */
@Tag(name = "用户端-认证", description = "文档 21.1：用户端短信/微信登录与验证码（/app/auth/*、/app/verifyCode）")
@RestController
@RequestMapping("/app/auth")
public class AppSmsAuthController {

    @Autowired
    private AppAuthService appAuthService;

    @Operation(summary = "短信验证码登录", description = "校验 phone+code+agreedProtocol，未注册自动注册并签发 JWT；返回 profile.ready 门禁。错误码：50001/350004/350011/350002/350019，见 docs/login-rules-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = LoginVO.class)))
    @PostMapping("sms")
    public Result<LoginVO> smsLogin(@RequestBody @Validated AppSmsLoginBO appSmsLoginBO) {
        return appAuthService.smsLogin(appSmsLoginBO);
    }
}
