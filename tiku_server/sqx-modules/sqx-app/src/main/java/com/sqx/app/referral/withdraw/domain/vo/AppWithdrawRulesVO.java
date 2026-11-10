package com.sqx.app.referral.withdraw.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端提现规则（最低/最高/手续费）
 */
@Data
public class AppWithdrawRulesVO {

    @Schema(description = "最低提现金额（元）")
    private BigDecimal minAmount;

    @Schema(description = "最高提现金额（元）")
    private BigDecimal maxAmount;

    @Schema(description = "提现手续费率，如 0.05 表示 5%")
    private BigDecimal feeRate;

    @Schema(description = "提现金额步进（元，须为整数倍）")
    private BigDecimal stepAmount;
}
