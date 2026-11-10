package com.sqx.common.pay.channel.paypal;

import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.Json;
import com.paypal.orders.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class PaypalPay {
    public static final Logger LOGGER = LoggerFactory.getLogger(PaypalPay.class);

    public static final String CAPTURE = "CAPTURE";

    /**
     * LOGIN。当客户单击PayPal Checkout时，客户将被重定向到页面以登录PayPal并批准付款。
     * BILLING。当客户单击PayPal Checkout时，客户将被重定向到一个页面，以输入信用卡或借记卡以及完成购买所需的其他相关账单信息
     * NO_PREFERENCE。当客户单击“ PayPal Checkout”时，将根据其先前的交互方式将其重定向到页面以登录PayPal并批准付款，或重定向至页面以输入信用卡或借记卡以及完成购买所需的其他相关账单信息使用PayPal。
     * 默认值：NO_PREFERENCE
     */
    public static final String LANDINGPAGE = "NO_PREFERENCE";
    /**
     * CONTINUE。将客户重定向到PayPal付款页面后，将出现“ 继续”按钮。当结帐流程启动时最终金额未知时，请使用此选项，并且您想将客户重定向到商家页面而不处理付款。
     * PAY_NOW。将客户重定向到PayPal付款页面后，出现“ 立即付款”按钮。当启动结帐时知道最终金额并且您要在客户单击“ 立即付款”时立即处理付款时，请使用此选项。
     */
    public static final String USERACTION = "CONTINUE";
    /**
     * GET_FROM_FILE。使用贝宝网站上客户提供的送货地址。
     * NO_SHIPPING。从PayPal网站编辑送货地址。推荐用于数字商品
     * SET_PROVIDED_ADDRESS。使用商家提供的地址。客户无法在PayPal网站上更改此地址
     */
    public static final String SHIPPINGPREFERENCE = "GET_FROM_FILE";

    /**
     * web支付（网页支付）
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @param mode mode
     * @param name 商品名称
     * @param orderNo 订单号
     * @param amount 金额
     * @param returnUrl 订单成功回调地址
     * @param cancelUrl 订单取消回调地址
     * @return
     * @throws IOException
     */
    public static String webPay(String clientId,
                                String clientSecret,
                                String mode,
                                String name,
                                String orderNo,
                                String amount,
                                String returnUrl,
                                String cancelUrl
    ) throws IOException {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(buildRequestBody(name, orderNo, amount, returnUrl, cancelUrl));
        HttpResponse<Order> response = null;
        try {
            response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
        } catch (IOException e1) {
            try {
                LOGGER.error("第1次调用paypal订单创建失败");
                response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
            } catch (Exception e) {
                try {
                    LOGGER.error("第2次调用paypal订单创建失败");
                    response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
                } catch (Exception e2) {
                    LOGGER.error("第3次调用paypal订单创建失败，失败原因：{}", e2.getMessage());
                }
            }
        }

        if (Objects.isNull(response)) {
            LOGGER.error("paypal返回值为空");
            throw new RuntimeException("交易失败");
        }

        if (201 != response.statusCode()) {
            LOGGER.error("paypal交易失败: {}", response.result());
            throw new RuntimeException("交易失败");
        }

        String approve = "";
        LOGGER.info("Status Code = {}, Status = {}, OrderID = {}, Intent = {}", response.statusCode(), response.result().status(), response.result().id(), response.result().checkoutPaymentIntent());
        for (LinkDescription link : response.result().links()) {
            LOGGER.info("Links-{}: {}    \tCall Type: {}", link.rel(), link.href(), link.method());
            if (Objects.equals("approve", link.rel())) {
                approve = link.href();
            }
        }

        String totalAmount = response.result().purchaseUnits().get(0).amountWithBreakdown().currencyCode() + ":" + response.result().purchaseUnits().get(0).amountWithBreakdown().value();
        LOGGER.info("Total Amount: {}", totalAmount);
        String json = new JSONObject(new Json().serialize(response.result())).toString(4);
        LOGGER.info("createOrder response body: {}", json);

        return approve;
    }

    /**
     * app支付
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @param mode mode
     * @param name 商品名称
     * @param orderNo 订单号
     * @param amount 金额
     * @param returnUrl 订单成功回调地址
     * @param cancelUrl 订单取消回调地址
     * @return
     * @throws IOException
     */
    public static String appPay(String clientId,
                                String clientSecret,
                                String mode,
                                String name,
                                String orderNo,
                                String amount,
                                String returnUrl,
                                String cancelUrl
    ) throws IOException {
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(buildRequestBody(name, orderNo, amount, returnUrl, cancelUrl));
        HttpResponse<Order> response = null;
        try {
            response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
        } catch (IOException e1) {
            try {
                LOGGER.error("第1次调用paypal订单创建失败");
                response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
            } catch (Exception e) {
                try {
                    LOGGER.error("第2次调用paypal订单创建失败");
                    response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
                } catch (Exception e2) {
                    LOGGER.error("第3次调用paypal订单创建失败，失败原因：{}", e2.getMessage());
                }
            }
        }

        if (Objects.isNull(response)) {
            LOGGER.error("paypal返回值为空");
            throw new RuntimeException("交易失败");
        }

        if (201 != response.statusCode()) {
            LOGGER.error("paypal交易失败: {}", response.result());
            throw new RuntimeException("交易失败");
        }

        LOGGER.info("Status Code = {}, Status = {}, OrderID = {}, Intent = {}", response.statusCode(), response.result().status(), response.result().id(), response.result().checkoutPaymentIntent());

        String totalAmount = response.result().purchaseUnits().get(0).amountWithBreakdown().currencyCode() + ":" + response.result().purchaseUnits().get(0).amountWithBreakdown().value();
        LOGGER.info("Total Amount: {}", totalAmount);
        String json = new JSONObject(new Json().serialize(response.result())).toString(4);
        LOGGER.info("createOrder response body: {}", json);

        return response.result().id();
    }

    /**
     * 用户授权支付成功，进行扣款操作
     * @param orderNo 订单编号
     * @param clientId clientId
     * @param clientSecret clientSecret
     * @param mode mode
     * @return
     * @throws IOException
     */
    public static HttpResponse<Order> captureOrder(String orderNo, String clientId, String clientSecret, String mode) throws IOException {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderNo);
        request.requestBody(new OrderRequest());
        HttpResponse<Order> response = null;

        try {
            response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
        } catch (IOException e1) {
            try {
                LOGGER.error("第1次调用paypal扣款失败");
                response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
            } catch (Exception e) {
                try {
                    LOGGER.error("第2次调用paypal扣款失败");
                    response = PayPalClient.client(mode, clientId, clientSecret).execute(request);
                } catch (Exception e2) {
                    LOGGER.error("第3次调用paypal扣款失败，失败原因 {}", e2.getMessage());
                }
            }
        }
        LOGGER.info("Status Code = {}, Status = {}, OrderID = {}", response.statusCode(), response.result().status(), response.result().id());
        for (LinkDescription link : response.result().links()) {
            LOGGER.info("Links-{}: {}    \tCall Type: {}", link.rel(), link.href(), link.method());
        }
        Payer buyer = response.result().payer();
        LOGGER.info("Buyer Email Address: {}", buyer.email());
        LOGGER.info("Buyer Name: {} {}", buyer.name().givenName(), buyer.name().surname());
        String json = new JSONObject(new Json().serialize(response.result())).toString(4);
        LOGGER.info("captureOrder response body: {}", json);

        return response;
    }

    /**
     * 生成订单主体信息
     * @param name 商品名称
     * @param orderNo 订单号
     * @param amount 金额
     * @param returnUrl 订单成功回调地址
     * @param cancelUrl 订单取消回调地址
     * @return 请求体
     */
    private static OrderRequest buildRequestBody(String name, String orderNo, String amount, String returnUrl, String cancelUrl) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent(CAPTURE);
        ApplicationContext applicationContext = new ApplicationContext()
                .brandName(name)
                .landingPage(LANDINGPAGE)
                .cancelUrl(cancelUrl).returnUrl(returnUrl)
                .userAction(USERACTION)
                .shippingPreference(SHIPPINGPREFERENCE);
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
        @SuppressWarnings("serial")
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .description(name)
                .customId(orderNo)
                .invoiceId(orderNo)
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode("USD")
                        .value(amount)// value = itemTotal + shipping + handling + taxTotal + shippingDiscount;
                        .amountBreakdown(new AmountBreakdown()
                                .itemTotal(new Money().currencyCode("USD").value(amount)) // itemTotal = Item[Supernote A6](value × quantity) + Item[帆布封套](value × quantity)
                                .shipping(new Money().currencyCode("USD").value("0.00"))
                                .handling(new Money().currencyCode("USD").value("0.00"))
                                .taxTotal(new Money().currencyCode("USD").value("0.00"))
                                .shippingDiscount(new Money().currencyCode("USD").value("0.00"))));
        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }
}
