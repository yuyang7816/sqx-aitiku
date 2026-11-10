package com.sqx.app.vip.pay.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 创建会员支付订单出参（POST /app/pay）
 */
@Data
public class AppPayCreateVO {

    @Schema(description = "业务订单号")
    private String orderNo;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "有效天数")
    private Integer planDays;

    @Schema(description = "订单金额（元）")
    private BigDecimal amount;

    @Schema(description = "支付方式 wechat/alipay")
    private String payMethod;

    @Schema(description = "微信 JSAPI 调起支付参数（uni.requestPayment 字段）")
    private Map<String, String> prepay;
}
