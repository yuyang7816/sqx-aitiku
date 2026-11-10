package com.sqx.app.base.auth.domain.vo;

import com.sqx.app.base.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端微信 OAuth 登录出参（POST /app/auth/wechat）
 */
@Data
@Schema(description = "微信 OAuth 登录出参（POST /app/auth/wechat）")
public class AppWechatLoginVO {

    @Schema(description = "JWT token")
    private String token;

    @Schema(description = "token 过期时间（秒）")
    private Long expire;

    @Schema(description = "用户信息")
    private UserEntity user;

    @Schema(description = "是否首次登录（新注册用户为 true）")
    private Boolean firstLogin;

    @Schema(description = "用户资料门禁状态（ready=false 时 uniapp 需跳转计划页，见 docs/login-rules-api.md）")
    private LoginProfileStateVO profile;

    @Schema(description = "小程序是否仍需授权手机号（true 时须调 POST /app/auth/wechat/bindPhone）")
    private Boolean needBindPhone;
}
