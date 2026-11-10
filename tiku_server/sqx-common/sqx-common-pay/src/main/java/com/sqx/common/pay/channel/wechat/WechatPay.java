package com.sqx.common.pay.channel.wechat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.pay.channel.wechat.domain.WxPayRequest;
import com.sqx.common.pay.channel.wechat.domain.WxRefundRequest;
import com.sqx.common.pay.channel.wechat.enums.WxTradeType;
import com.sqx.common.pay.channel.wechat.manager.WxPayServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/4/18
 */
@Slf4j
@Component
public class WechatPay {

    @Resource
    private WxPayServiceManager wxPayServiceManager;

    /**
     * 拉起微信支付
     * @return 跳转链接
     */
    public String pay(WxTradeType tradeType, WxPayRequest request) throws WxPayException {
        WxPayService wxPayService = wxPayServiceManager.getWxPayService(request.getAppId());
        WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
        orderRequest.setBody(request.getSubject());
        orderRequest.setOutTradeNo(request.getOrderNo());
        orderRequest.setTotalFee(BaseWxPayRequest.yuanToFen(String.valueOf(request.getAmount()))); //元转成分
        orderRequest.setSpbillCreateIp(request.getIp());
        orderRequest.setNotifyUrl(request.getNotifyUrl());
        orderRequest.setTradeType(request.getTradeType());
        orderRequest.setOpenid(request.getOpenId());

        Object result = wxPayService.createOrder(orderRequest);
        return beanToJson(result);
    }

    private String beanToJson(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("对象转 JSON 失败", e);
            throw new SqxException("支付参数生成失败", e);
        }
    }

    public String refund(WxRefundRequest request) throws WxPayException {
        WxPayService wxPayService = wxPayServiceManager.getWxPayService(request.getAppId());

        WxPayRefundRequest wxPayRefundRequest = WxPayRefundRequest.newBuilder()
                .outRefundNo(request.getRefundNo())
                .outTradeNo(request.getOrderNo())
                .refundFee(BaseWxPayRequest.yuan2Fen(request.getAmount()))
                .totalFee(BaseWxPayRequest.yuan2Fen(request.getAmount()))
                .refundDesc(request.getReason())
                .build();

        WxPayRefundResult result = wxPayService.refund(wxPayRefundRequest);
        return result.getRefundId();
    }
}
