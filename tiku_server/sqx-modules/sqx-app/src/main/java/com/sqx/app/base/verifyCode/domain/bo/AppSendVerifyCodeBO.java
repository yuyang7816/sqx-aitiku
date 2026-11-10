package com.sqx.app.base.verifyCode.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户端发送短信验证码入参
 */
@Data
public class AppSendVerifyCodeBO {

    @Schema(description = "手机号（11位，以1开头）")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的手机号")
    private String phone;
}
