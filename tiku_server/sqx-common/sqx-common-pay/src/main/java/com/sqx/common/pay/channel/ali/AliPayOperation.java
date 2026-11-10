package com.sqx.common.pay.channel.ali;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.domain.RefundRequest;
import com.sqx.common.pay.domain.UnifiedPayRequest;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.enums.PayTradeType;
import com.sqx.common.pay.notify.NotifyConstant;
import com.sqx.common.pay.operation.IPayOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Component
public class AliPayOperation implements IPayOperation {

    @Resource
    private AliPay aliPay;

    @Override
    public boolean support(String channel) {
        return Objects.equals(PayChannel.ALI.getValue(), channel);
    }

    @Override
    public String pay(UnifiedPayRequest request) throws WxPayException, AlipayApiException {
        if (Objects.equals(PayTradeType.H5.name(), request.getTradeType())) {
            return aliPay.wapPay(request.getOrderNo(), String.valueOf(request.getAmount()), request.getSubject(), request.getReturnUrl(), request.getNotifyUrl());
        }

        if (Objects.equals(PayTradeType.APP.name(), request.getTradeType())) {
            return aliPay.appPay(request.getOrderNo(), String.valueOf(request.getAmount()), request.getSubject(), request.getReturnUrl(), request.getNotifyUrl());
        }

        if (Objects.equals(PayTradeType.NATIVE.name(), request.getTradeType())) {
            return aliPay.pagePay(request.getOrderNo(), String.valueOf(request.getAmount()), request.getSubject(), request.getReturnUrl(), request.getNotifyUrl(), request.getQrPayMode(), request.getQrcodeWidth());
        }

        log.error("不支持的支付方式: {}", request.getTradeType());
        throw new BizException("不支持的支付方式");
    }

    @Override
    public String refund(RefundRequest request) throws WxPayException {
        return "";
    }
}
