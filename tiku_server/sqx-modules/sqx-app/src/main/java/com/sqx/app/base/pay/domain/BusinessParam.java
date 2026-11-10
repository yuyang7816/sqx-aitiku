package com.sqx.app.base.pay.domain;

import com.sqx.common.pay.enums.PayChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author chengming
 * @since 2025/4/22
 *
 * 业务参数
 */
@Data
public class BusinessParam {

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户openId")
    private String openId;

    @NotBlank(message = "业务类型不能为空")
    @Schema(name = "businessType", description = "业务类型")
    private String businessType;

    @NotBlank(message = "交易类型不能为空")
    @Schema(description = "交易类型 JSAPI-公众号支付/WEB/H5支付 NATIVE-原生扫码支付 APP-app支付")
    private String tradeType;

    @NotBlank(message = "支付成功后重定向uri不能为空")
    @Schema(name = "redirectUri", description = "支付成功后重定向uri")
    private String redirectUri;

    @NotNull(message = "订单金额不能为空")
    @Schema(name = "amount", description = "订单金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道", implementation = PayChannel.class)
    private String channel;

    /* 其他业务参数 */
    @Schema(description = "其他业务参数")
    private Map<String, Object> extensions;
}
