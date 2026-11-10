package com.sqx.common.pay.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Data
@Builder
public class UnifiedPayRequest {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单标题
     */
    private String subject;

    /**
     * 订单描述
     */
    private String description;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付渠道 com.sqx.common.pay.enums.PayChannel
     */
    private String channel;

    /**
     * 支付方式 com.sqx.common.pay.enums.TradeType
     */
    private String tradeType;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 跳转链接
     */
    private String returnUrl;

    /**
     * 客户端ip 微信必传参数
     */
    private String clientIp;

    /**
     * 用户openId 微信tradeType为JSAPI时必传参数
     */
    private String openId;

    /**
     * 支付宝扫码支付时可选
     * PC扫码支付的方式
     * 支持前置模式和跳转模式
     * 前置模式是将二维码前置到商户的订单确认页的模式。需要商户在自己的页面中以 iframe 方式请求支付宝页面。具体支持的枚举值有以下几种：
     * 0：订单码-简约前置模式，对应 iframe 宽度不能小于600px，高度不能小于300px；
     * 1：订单码-前置模式，对应iframe 宽度不能小于 300px，高度不能小于600px；
     * 3：订单码-迷你前置模式，对应 iframe 宽度不能小于 75px，高度不能小于75px；
     * 4：订单码-可定义宽度的嵌入式二维码，商户可根据需要设定二维码的大小
     * 跳转模式下，用户的扫码界面是由支付宝生成的，不在商户的域名下。支持传入的枚举值有：
     * 2：订单码-跳转模式")
     */
    private String qrPayMode;

    /**
     * 支付宝扫码支付时可选
     * 商户自定义二维码宽度 qr_pay_mode=4时该参数有效
     */
    private Long qrcodeWidth;

    private String appId;
}
