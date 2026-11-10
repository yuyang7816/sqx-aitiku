package com.sqx.common.pay.notify;


import com.sqx.common.pay.enums.PayChannel;

/**
 * @author chengming
 * @since 2025/12/25
 */
public interface IPayNotifyBusinessService {

    void onPaymentSuccess(String channel, String outTradeNo, String tradeNo);

    void onPaymentFailed(String channel, String outTradeNo, String tradeNo);
}
