package com.sqx.common.pay.constants;

import java.util.List;
import java.util.Map;

/**
 * @author chengming
 * @since 2026/2/11
 */
public interface PayConstant {

    // 微信
    interface Wx {
        String MCH_ID = "pay_wechat_mch_id";
        String MCH_KEY = "pay_wechat_mch_key";
        String KEY_PATH = "sys_wx_key_path";

        // 用户端
        interface User {
            // app appid
            String APP_ID = "sys_wechat_app_appid";

            // 公众号appid
            String MP_APP_ID = "sys_wechat_h5_appid";

            // 小程序appid
            String MA_APP_ID = "sys_wechat_jsapi_appid";
        }

        // 其他端
    }
}
