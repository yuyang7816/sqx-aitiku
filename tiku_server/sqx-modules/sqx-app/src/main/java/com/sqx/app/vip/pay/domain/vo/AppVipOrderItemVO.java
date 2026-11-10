package com.sqx.app.vip.pay.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户端会员订单列表项（GET /app/vipOrder）
 */
@Data
public class AppVipOrderItemVO {

    @Schema(description = "业务订单号")
    private String orderNo;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "有效天数")
    private Integer planDays;

    @Schema(description = "订单金额（元）")
    private BigDecimal amount;

    @Schema(description = "支付渠道 wechat/alipay")
    private String payChannel;

    @Schema(description = "支付状态 0待支付 1已支付 2已关闭")
    private Integer payStatus;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "支付成功时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
}
