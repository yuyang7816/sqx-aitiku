package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Data
public class PhoneRegisterBO {

    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phoneNo;

    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "手机类型 1安卓 2ios")
    private Integer sysPhone;

    @Schema(description = "来源 APP 小程序 公众号")
    private String platform;

    @Schema(description = "邀请人邀请码")
    private String superiorInvitationCode;
}
