package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端微信 OAuth 登录入参（POST /app/auth/wechat）
 */
@Data
public class AppWechatLoginBO {

    @Schema(description = "uni.login 返回的临时登录凭证 code")
    @NotBlank(message = "微信登录凭证不能为空")
    private String code;

    @Schema(description = "是否已同意用户协议与隐私政策（文档 23.1，须为 true）")
    private Boolean agreedProtocol;

    @Schema(description = "登录渠道：ma 小程序 / app 移动应用，默认 ma")
    private String channel;
}
