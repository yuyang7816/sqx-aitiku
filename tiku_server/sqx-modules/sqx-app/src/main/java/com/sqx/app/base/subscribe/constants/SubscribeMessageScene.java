package com.sqx.app.base.subscribe.constants;

/**
 * 微信订阅消息业务场景（与 sys_config 模板 ID 一一对应）
 */
public enum SubscribeMessageScene {

    /** 月度学习报告 */
    MONTHLY_REPORT("monthly_report", "月报"),

    /** 会员到期 / 续费提醒 */
    VIP_EXPIRE("vip_expire", "会员到期"),

    /** 待复习错题提醒 */
    WRONG_REVIEW("wrong_review", "错题提醒");

    private final String code;
    private final String desc;

    SubscribeMessageScene(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String code() {
        return code;
    }

    public String desc() {
        return desc;
    }

    public static SubscribeMessageScene fromCode(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim();
        for (SubscribeMessageScene scene : values()) {
            if (scene.code.equalsIgnoreCase(normalized)) {
                return scene;
            }
        }
        return null;
    }
}
