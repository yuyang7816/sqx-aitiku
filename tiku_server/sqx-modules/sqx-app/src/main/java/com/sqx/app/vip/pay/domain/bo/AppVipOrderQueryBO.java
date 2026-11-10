package com.sqx.app.vip.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端会员订单列表筛选（GET /app/vipOrder query）
 */
@Data
public class AppVipOrderQueryBO {

    @Schema(description = "业务订单号（模糊匹配）")
    private String orderNo;

    @Schema(description = "套餐名称")
    private String planName;

    @Schema(description = "支付状态 0待支付 1已支付 2已关闭")
    private Integer payStatus;
}
