package com.sqx.app.referral.withdraw.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-05 10:10:32
 */
@Data
public class WithdrawRecordSaveBo {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "提现账户 1微信 2支付宝")
    private Integer accountType;
}
