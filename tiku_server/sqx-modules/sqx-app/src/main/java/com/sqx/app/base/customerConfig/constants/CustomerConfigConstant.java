package com.sqx.app.base.customerConfig.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 客服配置常量（sys_config.param_key 与 legacy common/type ID 映射）
 */
public interface CustomerConfigConstant {

    /** 配置分组类型 */
    String PARAM_TYPE = "app_customer";

    /** 配置分组描述 */
    String PARAM_TYPE_DESC = "客服配置";

    /** 企业微信客服链接（legacy typeId=274） */
    String APP_KEFU_WEWORK_URL = "app_kefu_wework_url";

    /** 企业微信客服 AppId（legacy typeId=275） */
    String APP_KEFU_WEWORK_APPID = "app_kefu_wework_appid";

    /** 客服类型，如 wework / phone（legacy typeId=328） */
    String APP_KEFU_TYPE = "app_kefu_type";

    /** 客服电话（legacy typeId=329） */
    String APP_KEFU_PHONE = "app_kefu_phone";

    /** 在线 IM 开关（1=开启 0=关闭） */
    String APP_KEFU_IM_ENABLED = "app_kefu_im_enabled";

    /** 在线 IM 欢迎语 */
    String APP_KEFU_IM_WELCOME = "app_kefu_im_welcome";

    /** 客服微信号（legacy typeId=330） */
    String APP_KEFU_WECHAT_ID = "app_kefu_wechat_id";

    /** 客服微信二维码 URL（legacy typeId=331） */
    String APP_KEFU_WECHAT_QR_URL = "app_kefu_wechat_qr_url";

    /** 客服入口开关（与启动配置共用，1=展示 0=隐藏） */
    String APP_STARTUP_KEFU_ENABLED = "app_startup_kefu_enabled";

    /**
     * legacy GET /app/common/type/{id} 与 param_key 映射
     */
    Map<Long, String> LEGACY_TYPE_KEY_MAP = Collections.unmodifiableMap(new HashMap<Long, String>() {{
        put(274L, APP_KEFU_WEWORK_URL);
        put(275L, APP_KEFU_WEWORK_APPID);
        put(328L, APP_KEFU_TYPE);
        put(329L, APP_KEFU_PHONE);
        put(330L, APP_KEFU_WECHAT_ID);
        put(331L, APP_KEFU_WECHAT_QR_URL);
    }});
}
