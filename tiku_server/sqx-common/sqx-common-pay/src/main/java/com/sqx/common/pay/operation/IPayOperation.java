package com.sqx.common.pay.operation;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.sqx.common.pay.domain.RefundRequest;
import com.sqx.common.pay.domain.UnifiedPayRequest;

/**
 * @author chengming
 * @since 2026/1/9
 */
public interface IPayOperation {

    boolean support(String channel);

    /**
     * 拉起支付
     * @param request 支付参数
     * @return 跳转链接
     */
    String pay(UnifiedPayRequest request) throws WxPayException, AlipayApiException;

    /**
     * 发起退款
     * @param request 退款参数
     */
    String refund(RefundRequest request) throws WxPayException;
}