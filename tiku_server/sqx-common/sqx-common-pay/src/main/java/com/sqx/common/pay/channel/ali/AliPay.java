package com.sqx.common.pay.channel.ali;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.AlipayResponse;
import com.alipay.api.diagnosis.DiagnosisUtils;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.alipay.service.schema.util.StringUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.channel.ali.enums.AliMode;
import com.sqx.common.pay.channel.ali.utils.AlipayConfigUtil;
import com.sqx.common.pay.enums.PayConfigCache;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/4/21
 *
 * 支付宝支付
 */
@Slf4j
@Component
public class AliPay {
    /**
     * wap支付 （手机网站支付）
     * @param outTradeNo 订单号
     * @param amount 金额
     * @param subject 标题
     * @param returnUrl 异步通知url
     * @param notifyUrl 同步通知url
     * @return 跳转链接
     */
    public String wapPay(String outTradeNo, String amount, String subject, String returnUrl, String notifyUrl) throws AlipayApiException {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);

        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        // 设置商户订单号
        model.setOutTradeNo(outTradeNo);
        // 设置订单总金额
        model.setTotalAmount(amount);
        // 设置订单标题
        model.setSubject(subject);
        // 设置产品码
        model.setProductCode("QUICK_WAP_WAY");

        request.setBizModel(model);

        AlipayClient alipayClient = AlipayConfigUtil.getAlipayClient();
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        checkResponse(response);

        return response.getBody();
    }

    /**
     * page 支付 （电脑网站支付/扫码支付）
     * @param outTradeNo 订单号
     * @param amount 金额
     * @param subject 标题
     * @param returnUrl 异步通知url
     * @param notifyUrl 同步通知url
     * @param qrPayMode PC扫码支付的方式
     *  支持前置模式和跳转模式。
     *  前置模式是将二维码前置到商户的订单确认页的模式。需要商户在自己的页面中以 iframe 方式请求支付宝页面。具体支持的枚举值有以下几种：
     *  0：订单码-简约前置模式，对应 iframe 宽度不能小于600px，高度不能小于300px；
     *  1：订单码-前置模式，对应iframe 宽度不能小于 300px，高度不能小于600px；
     *  3：订单码-迷你前置模式，对应 iframe 宽度不能小于 75px，高度不能小于75px；
     *  4：订单码-可定义宽度的嵌入式二维码，商户可根据需要设定二维码的大小。
     *  跳转模式下，用户的扫码界面是由支付宝生成的，不在商户的域名下。支持传入的枚举值有：
     *  2：订单码-跳转模式
     * @param qrcodeWidth 商户自定义二维码宽度 qr_pay_mode=4时该参数有效
     * @return 跳转链接
     */
    public String pagePay(String outTradeNo, String amount, String subject, String returnUrl, String notifyUrl, String qrPayMode, Long qrcodeWidth) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        // 设置商户订单号
        model.setOutTradeNo(outTradeNo);
        // 设置订单总金额
        model.setTotalAmount(amount);
        // 设置订单标题
        model.setSubject(subject);
        // 设置产品码
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 设置PC扫码支付的方式
        model.setQrPayMode(StringUtil.isEmpty(qrPayMode) ? "2" : qrPayMode);
        // 设置商户自定义二维码宽度
        model.setQrcodeWidth(Objects.isNull(qrcodeWidth) ? 100 : qrcodeWidth);

        request.setBizModel(model);

        AlipayClient alipayClient = AlipayConfigUtil.getAlipayClient();
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
        checkResponse(response);

        return response.getBody();
    }

    /**
     * app 支付
     * @param outTradeNo 订单号
     * @param amount 金额
     * @param subject 标题
     * @param returnUrl 异步通知url
     * @param notifyUrl 同步通知url
     * @return 跳转链接
     */
    public String appPay(String outTradeNo, String amount, String subject, String returnUrl, String notifyUrl) throws AlipayApiException {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        // 设置商户订单号
        model.setOutTradeNo(outTradeNo);
        // 设置订单总金额
        model.setTotalAmount(amount);
        // 设置订单标题
        model.setSubject(subject);
        // 设置产品码
        model.setProductCode("QUICK_MSECURITY_PAY");

        request.setBizModel(model);

        AlipayClient alipayClient = AlipayConfigUtil.getAlipayClient();
        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
        checkResponse(response);

        return response.getBody();
    }

    private void checkResponse(AlipayResponse response) throws AlipayApiException {
        if (!response.isSuccess()) {
            log.error("支付宝交易失败, 错误码: {} 错误信息{}", response.getCode(), response.getMsg());

            String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            log.error("诊断链接: {}", diagnosisUrl);

            throw new RuntimeException("交易失败");
        }
    }

    public boolean verifySign(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return false;
        }

        String mode = ConfigUtil.getConfigValue(PayConfigCache.ALI_MODE.getCode());
        try {
            if (Objects.equals(AliMode.PUBLIC_KEY.getCode(), mode)) {
                String publicKey = ConfigUtil.getConfigValue(PayConfigCache.ALI_PUBLIC_KEY.getCode());

                return AlipaySignature.rsaCheckV1(
                        params,
                        publicKey,
                        AlipayConstants.CHARSET_UTF8,
                        AlipayConstants.SIGN_TYPE_RSA2
                );
            }

            if (Objects.equals(AliMode.CERT.getCode(), mode)) {
                String publicCertPath = ConfigUtil.getConfigValue(PayConfigCache.ALI_ALIPAY_PUBLIC_CERT_PATH.getCode());

                return AlipaySignature.rsaCertCheckV1(
                        params,
                        publicCertPath,
                        AlipayConstants.CHARSET_UTF8,
                        AlipayConstants.SIGN_TYPE_RSA2
                );
            }

            log.error("未配置支付宝支付模式");
            throw new BizException("支付宝验签失败");
        } catch (AlipayApiException e) {
            log.error("支付宝验签失败", e);
            return false;
        }
    }
}
