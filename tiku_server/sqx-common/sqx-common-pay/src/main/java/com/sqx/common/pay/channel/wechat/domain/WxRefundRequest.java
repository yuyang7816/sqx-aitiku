package com.sqx.common.pay.channel.wechat.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Data
@Builder
public class WxRefundRequest {

    /**
     * appid
     */
    private String appId;

    /**
     * 退款编号
     */
    private String refundNo;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单总金额
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
}
