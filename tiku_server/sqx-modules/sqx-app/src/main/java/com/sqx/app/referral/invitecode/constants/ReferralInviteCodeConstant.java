package com.sqx.app.referral.invitecode.constants;

/**
 * 推广邀请码与小程序码常量
 */
public final class ReferralInviteCodeConstant {

    private ReferralInviteCodeConstant() {
    }

    /**
     * 扫码进入 uniapp 首页，由 App.onLaunch 解析 scene 中的 inviteCode
     */
    public static final String MA_ENTRY_PAGE = "pages/index/index";

    /**
     * 微信 scene 最大长度
     */
    public static final int SCENE_MAX_LENGTH = 32;

    /**
     * 邀请码生成冲突重试次数
     */
    public static final int INVITE_CODE_MAX_RETRY = 10;

    /**
     * 本地存储小程序码相对目录（位于 file_local_upload_path 下）
     */
    public static final String QR_RELATIVE_DIR = "referral/qr";
}
