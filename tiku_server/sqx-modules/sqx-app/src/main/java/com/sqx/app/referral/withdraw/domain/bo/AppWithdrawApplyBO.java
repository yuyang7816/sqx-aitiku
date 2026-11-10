package com.sqx.app.referral.withdraw.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * POST /app/withdrawRecord 提现申请入参
 */
@Data
public class AppWithdrawApplyBO {

    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额必须大于0")
    @Schema(description = "提现金额（元）", required = true, example = "100")
    private BigDecimal amount;

    @Schema(description = "提现渠道：1=微信零钱（默认），2=支付宝", example = "1")
    private Integer accountType;
}
