package com.sqx.app.referral.constants;

/**
 * @author chengming
 * @since 2026/2/6
 */
public interface ReferralConstant {

    interface commission {
        /** 一级推广佣金比例（直接邀请人） */
        String SYS_COMMISSION_RATE_LEVEL1 = "biz_referral_commission_rate_level1";

        /** 二级推广佣金比例（邀请人的上级） */
        String SYS_COMMISSION_RATE_LEVEL2 = "biz_referral_commission_rate_level2";

        /**
         * 兼容旧配置键（等同一级佣金）
         */
        String SYS_COMMISSION_RATE = "biz_referral_commission_rate";
    }

    interface withdraw {
        /**
         * 最大提现金额
         */
        String SYS_WITHDRAW_MAX = "sys_withdraw_max";

        /**
         * 最小提现金额
         */
        String SYS_WITHDRAW_MIN = "sys_withdraw_min";

        /**
         * 提现费率
         */
        String SYS_WITHDRAW_RATE = "sys_withdraw_rate";
    }

    interface poster {
        /** 推广邀请海报底图（管理端上传） */
        String SYS_POSTER_IMAGE = "biz_referral_poster_image";

        /** 推广邀请语（海报底部与分享文案） */
        String SYS_INVITE_TEXT = "sys_invite_text";
    }

}
