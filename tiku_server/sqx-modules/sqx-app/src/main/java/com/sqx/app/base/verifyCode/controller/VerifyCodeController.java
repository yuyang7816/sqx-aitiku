package com.sqx.app.base.verifyCode.controller;

import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.verifyCode.domain.bo.SendEmailCodeBO;
import com.sqx.app.base.verifyCode.domain.bo.SendPhoneCodeBO;
import com.sqx.app.base.verifyCode.domain.entity.VerifyCode;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
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
 * 验证码 前端控制器
 *
 * @author sqx
 * @since 2025-04-22 15:31:50
 */
@Tag(name = "验证码", description = "验证码")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/verifyCode")
public class VerifyCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Operation(summary = "发送手机验证码")
    @PostMapping("sendPhoneCode")
    public Result<VerifyCode> sendPhoneCode(@RequestBody @Validated SendPhoneCodeBO sendPhoneCodeBO) {
        VerifyCode verifyCode = verifyCodeService.sendPhoneCode(sendPhoneCodeBO.getPhone(), sendPhoneCodeBO.getBizCode());
        return Result.success(verifyCode);
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("sendEmailCode")
    public Result<VerifyCode> sendEmailCode(@RequestBody @Validated SendEmailCodeBO sendEmailCodeBO) {
        VerifyCode verifyCode = verifyCodeService.sendMailCode(sendEmailCodeBO.getEmail(), sendEmailCodeBO.getBizCode());
        return Result.success(verifyCode);
    }
}
