package com.sqx.app.vip.pay.service;

/**
 * 支付成功后的会员开通
 */
public interface VipPayNotifyService {

    /**
     * 微信支付回调验签成功后开通/续费 VIP
     *
     * @param outTradeNo 商户订单号
     * @param tradeNo    微信支付流水号
     */
    void onWechatPaySuccess(String outTradeNo, String tradeNo);
}
