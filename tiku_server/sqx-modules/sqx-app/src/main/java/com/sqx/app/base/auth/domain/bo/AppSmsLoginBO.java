package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户端短信验证码登录入参（POST /app/auth/sms）
 */
@Data
public class AppSmsLoginBO {

    @Schema(description = "手机号（11位，以1开头）")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的手机号")
    private String phone;

    @Schema(description = "短信验证码（4~6位数字）")
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4,6}$", message = "请输入4~6位数字验证码")
    private String code;

    @Schema(description = "是否已同意用户协议与隐私政策（文档 23.1，须为 true）")
    private Boolean agreedProtocol;
}
