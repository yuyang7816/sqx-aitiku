package com.sqx.app.wallet.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端钱包余额（GET /app/userWallet）
 */
@Data
public class AppUserWalletBalanceVO {

    @Schema(description = "可提现余额（单位：元）")
    private BigDecimal withdrawableAmount;
}
