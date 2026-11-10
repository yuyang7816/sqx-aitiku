package com.sqx.app.base.pay.business.service.impl;

import com.sqx.app.vip.pay.service.VipPayNotifyService;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.notify.IPayNotifyBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 支付回调业务：验签成功后开通 VIP
 */
@Slf4j
@Service
public class PayNotifyBusinessServiceImpl implements IPayNotifyBusinessService {

    @Resource
    private VipPayNotifyService vipPayNotifyService;

    @Override
    public void onPaymentSuccess(String channel, String outTradeNo, String tradeNo) {
        log.info("支付回调成功 channel={} outTradeNo={} tradeNo={}", channel, outTradeNo, tradeNo);
        if (Objects.equals(channel, PayChannel.WX.getValue())) {
            vipPayNotifyService.onWechatPaySuccess(outTradeNo, tradeNo);
        }
    }

    @Override
    public void onPaymentFailed(String channel, String outTradeNo, String tradeNo) {
        log.warn("支付回调失败 channel={} outTradeNo={} tradeNo={}", channel, outTradeNo, tradeNo);
    }
}
