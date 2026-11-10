package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/8/6
 */
@Data
public class WxJsRegisterBo {

    @Schema(description = "openid")
    private String openId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邀请人邀请码")
    private String superiorInvitationCode;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;
}
