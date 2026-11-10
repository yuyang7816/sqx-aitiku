package com.sqx.common.pay.service.impl;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.sqx.common.pay.domain.RefundRequest;
import com.sqx.common.pay.domain.UnifiedPayRequest;
import com.sqx.common.pay.factory.PayOperationFactory;
import com.sqx.common.pay.notify.IPayNotifyHandler;
import com.sqx.common.pay.operation.IPayOperation;
import com.sqx.common.pay.service.IPayService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Service
public class IPayServiceImpl implements IPayService {

    @Resource
    private PayOperationFactory payOperationFactory;

    @Override
    public String unifiedPay(UnifiedPayRequest request) throws WxPayException, AlipayApiException {
        IPayOperation payOperation = payOperationFactory.getPayOperation(request.getChannel());
        return payOperation.pay(request);
    }

    @Override
    public String refund(RefundRequest request) throws WxPayException {
        IPayOperation payOperation = payOperationFactory.getPayOperation(request.getChannel());
        return payOperation.refund(request);
    }

    @Override
    public String notify(HttpServletRequest request, String channel) {
        IPayNotifyHandler payNotifyHandler = payOperationFactory.getPayNotifyHandler(channel);
        return payNotifyHandler.handler(request);
    }
}
