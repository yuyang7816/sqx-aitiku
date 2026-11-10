package com.sqx.app.vip.plan.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:51:13
 */
@Data
public class VipPlanUpdateBo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "会员名称")
    private String name;

    @Schema(description = "月数")
    private Integer month;

    @Schema(description = "会员价格")
    private BigDecimal amount;

    @Schema(description = "原价")
    private BigDecimal originalAmount;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
