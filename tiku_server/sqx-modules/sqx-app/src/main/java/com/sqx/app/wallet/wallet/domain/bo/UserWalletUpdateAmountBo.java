package com.sqx.app.wallet.wallet.domain.bo;

import com.sqx.app.wallet.wallet.enums.Operator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2026/1/16
 */
@Data
public class UserWalletUpdateAmountBo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "操作 增加=incr，减少=decr", implementation = Operator.class)
    private String operator;

    @Schema(description = "金额")
    private BigDecimal amount;
}
