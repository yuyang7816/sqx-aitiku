package com.sqx.app.wallet.transaction.domain.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端收益流水详情（GET /app/userWalletTransaction）
 */
@Data
public class AppUserWalletTransactionDetailVO {

    @Schema(description = "累计收益（有效收入合计：已到账 + 待结算，元）")
    private BigDecimal totalAmount;

    @Schema(description = "待结算收益（待处理收入合计，元）")
    private BigDecimal pendingAmount;

    @Schema(description = "收益流水分页列表")
    private IPage<AppUserWalletTransactionItemVO> records;
}
