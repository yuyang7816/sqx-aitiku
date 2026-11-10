package com.sqx.common.pay.channel.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengming
 * @since 2025/4/15
 */
public class StripePay {
    private static Logger LOGGER = LoggerFactory.getLogger(StripePay.class);

    /**
     * web支付
     * @param name 商品名称
     * @param amount 金额
     * @param successUrl 订单成功后跳转地址
     * @param cancelUrl 订单取消后跳转地址
     * @return session
     */
    private static String webPay(String orderNo, String name, BigDecimal amount, String apiKey, String successUrl, String cancelUrl) throws StripeException {
        Stripe.apiKey = apiKey;

        // 自定义业务参数
        Map<String, String> metaData = new HashMap<>();
        metaData.put("orderNo", orderNo);

        // 商品信息
        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(name)  // 商品名称
                .build();

        // 金额信息
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setUnitAmount(amount.longValue()) // 金额
                .setProductData(productData)
                .setCurrency("usd") // 货币
                .build();

        // line信息
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPriceData(priceData)
                .setQuantity(1L) // 商品数量
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(lineItem)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putAllMetadata(metaData)
                .build();

        Session session = Session.create(params);
        LOGGER.info("stripe 返回值: {}", session.getUrl());
        return session.getUrl();
    }

    /**
     * app支付
     * @param orderNo 订单号
     * @param amount 金额
     * @return
     * @throws StripeException
     */
    private static String appPay(String orderNo, BigDecimal amount, String apiKey) throws StripeException {
        Stripe.apiKey = apiKey;

        // 自定义业务参数
        Map<String, String> metaData = new HashMap<>();
        metaData.put("orderNo", orderNo);

        PaymentIntentCreateParams.AutomaticPaymentMethods automaticPaymentMethods = PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                .setEnabled(true)
                .build();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.longValue())
                .setCurrency("usd")
                .putAllMetadata(metaData)
                .setAutomaticPaymentMethods(automaticPaymentMethods)
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        LOGGER.info("stripe 返回值: {}", paymentIntent.getClientSecret());
        return paymentIntent.getClientSecret();
    }
}
