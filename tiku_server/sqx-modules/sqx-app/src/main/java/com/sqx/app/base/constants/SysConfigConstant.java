package com.sqx.app.base.constants;

/**
 * @author chengming
 * @since 2025/12/19
 */
public interface SysConfigConstant {

    /**
     * 系统配置
     */
    interface Sys {
        /**
         * 用户端小程序appid
         */
        String SYS_USER_MA_APPID = "sys_user_ma_appid";

        /**
         * 用户端小程序secret
         */
        String SYS_USER_MA_SECRET = "sys_user_ma_secret";

        /**
         * 用户端公众号 appid（H5 微信内置浏览器 OAuth）
         */
        String SYS_USER_MP_APPID = "sys_user_mp_appid";

        /**
         * 用户端公众号 secret
         */
        String SYS_USER_MP_SECRET = "sys_user_mp_secret";

        /**
         * 是否开启 H5 公众号 OAuth 登录（0 关 1 开，默认关）
         */
        String SYS_USER_MP_LOGIN_ENABLED = "sys_user_mp_login_enabled";

        /**
         * 用户端移动应用 appid（APP 微信登录/支付，可选）
         */
        String SYS_USER_APP_APPID = "sys_user_app_appid";

        /**
         * 用户端移动应用 secret
         */
        String SYS_USER_APP_SECRET = "sys_user_app_secret";

        /**
         * h5服务域名配置
         */
        String SYS_H5_DOMAIN = "sys_h5_domain";
    }
}
