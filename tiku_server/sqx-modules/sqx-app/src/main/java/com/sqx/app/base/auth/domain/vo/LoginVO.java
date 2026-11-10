package com.sqx.app.base.auth.domain.vo;

import com.sqx.app.base.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/4/22
 */
@Data
@Schema(description = "短信验证码登录出参（POST /app/auth/sms）")
public class LoginVO {

    @Schema(description = "token")
    private String token;

    @Schema(description = "token过期时间")
    private Long expire;

    @Schema(description = "用户信息")
    private UserEntity user;

    @Schema(description = "资料门禁状态（ready=false 时 uniapp 跳转学习计划页，见 docs/login-rules-api.md）")
    private LoginProfileStateVO profile;
}
