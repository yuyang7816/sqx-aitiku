package com.sqx.common.pay.channel.ali.notify;

import com.sqx.common.pay.channel.ali.AliPay;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.notify.IPayNotifyBusinessService;
import com.sqx.common.pay.notify.IPayNotifyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Component
public class AlipayNotifyHandler implements IPayNotifyHandler {

    @Resource
    private AliPay aliPay;
    @Resource
    private ObjectProvider<IPayNotifyBusinessService> businessServiceProvider;

    @Override
    public boolean support(String channel) {
        return Objects.equals(channel, PayChannel.ALI.getValue());
    }

    @Override
    public String handler(HttpServletRequest request) {
        log.info("进入支付宝回调");
        Map<String, String> params = requestToMap(request);

        if (params.isEmpty()) {
            return "failed";
        }

        // 回调验签
        boolean signVerified = aliPay.verifySign(params);

        if (!signVerified) {
            log.error("支付宝回调验证失败");
            return "failed";
        }

        String tradeNo = params.get("trade_no");
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");

        IPayNotifyBusinessService businessService = businessServiceProvider.getIfAvailable();
        if (Objects.isNull(businessService)) {
            log.error("未找到 IPaymentNotifyBusinessService 实现，请配置");
            return "failed";
        }

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            businessService.onPaymentSuccess(PayChannel.ALI.getValue(), outTradeNo, tradeNo);
        } else {
            businessService.onPaymentFailed(PayChannel.ALI.getValue(), outTradeNo, tradeNo);
        }

        return "success";
    }

    /**
     * 请求头转map
     * @param request 请求头
     * @return map
     */
    public static Map<String, String> requestToMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }

            params.put(name, valueStr);
        }

        return params;
    }
}
