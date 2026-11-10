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
public class WxPayRequest {

    private String appId;
    private String orderNo;
    private String subject;
    private BigDecimal amount;
    private String tradeType;
    private String notifyUrl;
    private String ip;
    private String openId;
}
