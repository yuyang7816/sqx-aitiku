package com.sqx.app.referral.invitecode.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端推广邀请码与带参小程序码（GET /app/referral/inviteCode）
 */
@Data
public class AppReferralInviteCodeVO {

    /**
     * 当前用户唯一邀请码（对应 tb_user.invitation_code）
     */
    @Schema(description = "邀请码")
    private String inviteCode;

    /**
     * 带 scene(inviteCode) 的小程序码图片访问地址
     */
    @Schema(description = "带参小程序码图片 URL")
    private String qrCodeUrl;

    /**
     * 管理端配置的邀请海报底图 URL
     */
    @Schema(description = "邀请海报底图 URL（sys_config.biz_referral_poster_image）")
    private String posterImageUrl;

    /**
     * 管理端配置的邀请语（sys_config.sys_invite_text）
     */
    @Schema(description = "邀请语")
    private String inviteText;
}
