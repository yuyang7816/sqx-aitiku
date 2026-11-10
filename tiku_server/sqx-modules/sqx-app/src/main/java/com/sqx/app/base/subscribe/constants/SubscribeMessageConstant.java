package com.sqx.app.base.subscribe.constants;

/**
 * 微信订阅消息：sys_config 键、小程序跳转页、模板字段名
 */
public final class SubscribeMessageConstant {

    private SubscribeMessageConstant() {
    }

    /** sys_config：月报模板 ID */
    public static final String KEY_MONTHLY_REPORT_TEMPLATE = "wx_ma_subscribe_monthly_report_template_id";

    /** sys_config：会员到期模板 ID */
    public static final String KEY_VIP_EXPIRE_TEMPLATE = "wx_ma_subscribe_vip_expire_template_id";

    /** sys_config：错题提醒模板 ID */
    public static final String KEY_WRONG_REVIEW_TEMPLATE = "wx_ma_subscribe_wrong_review_template_id";

    /** 月报订阅消息跳转页（不含前缀 /） */
    public static final String PAGE_MONTHLY_REPORT = "package/profile/analysis-report";

    /** 会员中心跳转页 */
    public static final String PAGE_VIP_MEMBER = "package/member/center";

    /** 错题本 Tab 页 */
    public static final String PAGE_WRONG_REVIEW = "pages/notebook/notebook";

    /** 模板字段：标题 / 摘要类（thing，最多 20 字） */
    public static final String FIELD_TITLE = "thing1";

    /** 模板字段：时间类（time，格式 yyyy-MM-dd HH:mm） */
    public static final String FIELD_TIME = "time2";

    /** 模板字段：日期类（date，格式 yyyy-MM-dd） */
    public static final String FIELD_DATE = "date2";

    /** 模板字段：数字类（number，纯数字） */
    public static final String FIELD_NUMBER = "number2";

    /** 模板字段：备注 / 说明类（thing，最多 20 字） */
    public static final String FIELD_REMARK = "thing3";

    /** 待复习错题超过 N 天未练习视为 stale */
    public static final int STALE_WRONG_DAYS = 3;
}
