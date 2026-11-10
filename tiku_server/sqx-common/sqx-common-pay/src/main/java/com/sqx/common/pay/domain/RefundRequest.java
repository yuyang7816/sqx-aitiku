package com.sqx.common.pay.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Data
public class RefundRequest {

    /**
     * 支付渠道
     */
    private String channel;

    /**
     * appId
     */
    private String appId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款回调地址
     */
    private String notifyUrl;
}
