package com.sqx.app.referral.members.constants;

/**
 * 推广下级成员列表常量
 */
public final class ReferralMemberConstant {

    private ReferralMemberConstant() {
    }

    /** 列表最多返回条数 */
    public static final int MAX_MEMBERS = 50;

    /** 已付费（开通会员） */
    public static final String STATUS_PAID = "已付费";

    /** 未付费（仅注册） */
    public static final String STATUS_UNPAID = "未付费";
}
