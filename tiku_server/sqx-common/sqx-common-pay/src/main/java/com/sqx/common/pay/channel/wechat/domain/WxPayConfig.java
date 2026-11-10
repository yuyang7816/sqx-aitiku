package com.sqx.common.pay.channel.wechat.domain;

import lombok.Data;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Data
public class WxPayConfig {

    /**
     * APP appid
     */
    private String appId;

    /**
     * jsapi appid
     */
    private String jsAppId;

    /**
     * h5 appid
     */
    private String h5AppId;

    /**
     * 商户ID
     */
    private String mchId;

    /**
     * 商户key
     */
    private String mchKey;

    /**
     * v2版本p12证书路径
     */
    private String keyPath;
}
