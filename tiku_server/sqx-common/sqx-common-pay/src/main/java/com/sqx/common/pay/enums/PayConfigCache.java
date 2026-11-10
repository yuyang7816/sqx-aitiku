package com.sqx.common.pay.enums;

/**
 * @author chengming
 * @since 2025/4/21
 * 本系统支付业务配置枚举
 */
public enum PayConfigCache {

    /* ======= 支付宝 ====== */
    ALI_APPID("pay_ali_appid", "支付宝appid"),
    ALI_PUBLIC_KEY("pay_ali_public_key", "支付宝公钥"),
    ALI_PRIVATE_KEY("pay_ali_private_key", "支付宝私钥"),
    ALI_APP_CERT_PATH("pay_ali_app_cert_path", "应用公钥证书路径"),
    ALI_ALIPAY_PUBLIC_CERT_PATH("pay_ali_alipay_public_cert_path", "支付宝公钥证书路径"),
    ALI_ROOT_CERT_PATH("pay_ali_root_cert_path", "支付宝根证书路径"),
    ALI_MODE("pay_ali_mode", "支付宝支付模式 1: 普通公钥模式 2: 证书模式"),
    ALI_SANDBOX("pay_ali_sandbox", "支付宝是否开启沙箱模式 1: 是 2: 否"),

    /* ======= 微信 ====== */
    WX_APP_ID("pay_wx_app_id", "微信支付app appid"),
    WX_H5_APP_ID("pay_wx_h5_app_id", "微信支付h5 appid"),
    WX_JSAPI_APP_ID("pay_wx_jsapi_app_id", "微信支付jsapi appid"),
    WX_MCH_ID("pay_wx_mch_id", "微信支付商户号"),
    WX_MCH_KEY("pay_wx_mch_key", "微信支付商户秘钥"),
    WX_KEY_PATH("pay_wx_key_path", "微信支付p12证书路径"),

    ;

    private final String code;
    private final String desc;

    PayConfigCache(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
