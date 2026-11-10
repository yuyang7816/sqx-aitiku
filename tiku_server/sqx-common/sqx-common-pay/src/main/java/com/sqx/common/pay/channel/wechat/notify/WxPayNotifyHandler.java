package com.sqx.common.pay.channel.wechat.notify;

import cn.hutool.core.util.XmlUtil;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.sqx.common.pay.channel.wechat.manager.WxPayServiceManager;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.notify.IPayNotifyBusinessService;
import com.sqx.common.pay.notify.IPayNotifyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Component
public class WxPayNotifyHandler implements IPayNotifyHandler {

    @Resource
    private ObjectProvider<IPayNotifyBusinessService> businessServiceProvider;
    @Resource
    private WxPayServiceManager wxPayServiceManager;

    @Override
    public boolean support(String channel) {
        return Objects.equals(channel, PayChannel.WX.getValue());
    }

    @Override
    public String handler(HttpServletRequest request) {
        try {
            String xml = requestToXml(request);
            Map<String, Object> map = XmlUtil.xmlToMap(xml);
            String appid = (String) map.get("appid");

            WxPayService wxPayService = wxPayServiceManager.getWxPayService(appid);
            WxPayOrderNotifyResult result = wxPayService.parseOrderNotifyResult(xml);

            IPayNotifyBusinessService businessService = businessServiceProvider.getIfAvailable();
            if (Objects.isNull(businessService)) {
                log.error("未找到 IPaymentNotifyBusinessService 实现，请配置");
                return WxPayNotifyResponse.fail("failed");
            }

            businessService.onPaymentSuccess(PayChannel.WX.getValue(), result.getOutTradeNo(), result.getTransactionId());
        } catch (Exception e) {
            log.error("微信回调结果异常,异常原因{}", e.getMessage());
            return WxPayNotifyResponse.fail(e.getMessage());
        }

        return WxPayNotifyResponse.success("success");
    }

    /**
     * 请求头转xml
     * @param request 请求头
     * @return xml
     */
    public static String requestToXml(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null; ) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}