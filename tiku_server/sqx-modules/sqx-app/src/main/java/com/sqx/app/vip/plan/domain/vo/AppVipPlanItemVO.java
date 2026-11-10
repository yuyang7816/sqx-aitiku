package com.sqx.app.vip.plan.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端会员套餐项（GET /app/vipPlan 出参，对齐 uniapp 月卡/季卡展示）
 */
@Data
public class AppVipPlanItemVO {

    @Schema(description = "套餐名称，如月卡、季卡")
    private String name;

    @Schema(description = "售价（元）")
    private BigDecimal price;

    @Schema(description = "有效天数")
    private Integer days;
}
