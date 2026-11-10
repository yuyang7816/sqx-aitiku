package com.sqx.common.pay.service;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.sqx.common.pay.domain.RefundRequest;
import com.sqx.common.pay.domain.UnifiedPayRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author chengming
 * @since 2026/1/9
 */
public interface IPayService {

    /**
     * 统一支付
     * @param request 统一支付参数
     * @return 统一支付参数返回值
     */
    String unifiedPay(UnifiedPayRequest request) throws WxPayException, AlipayApiException;

    String refund(RefundRequest request) throws WxPayException;

    String notify(HttpServletRequest request, String channel);
}
