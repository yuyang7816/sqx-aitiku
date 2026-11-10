package com.sqx.app.base.verifyCode.controller;

import com.sqx.app.base.verifyCode.domain.bo.AppSendVerifyCodeBO;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端短信验证码（uniapp 登录）
 */
@Tag(name = "用户端-认证", description = "文档 21.1：用户端短信/微信登录与验证码（/app/auth/*、/app/verifyCode）")
@RestController
@RequestMapping("/app/verifyCode")
public class AppVerifyCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Operation(summary = "发送短信验证码", description = "60 秒内同一手机号仅可发送一次；开发 Mock 见 sqx.sms.mock。错误码：50001/350010，见 docs/auth-api.md")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，data 为 null")
    })
    @PostMapping
    public Result<Void> send(@RequestBody @Validated AppSendVerifyCodeBO sendVerifyCodeBO) {
        verifyCodeService.sendAppVerifyCode(sendVerifyCodeBO.getPhone());
        return Result.success();
    }
}
