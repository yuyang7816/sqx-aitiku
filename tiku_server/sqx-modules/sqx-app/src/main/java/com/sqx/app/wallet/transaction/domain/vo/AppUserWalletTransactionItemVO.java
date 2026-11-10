package com.sqx.app.wallet.transaction.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端收益流水单条记录（兼容 uniapp promoIncomeRecords）
 */
@Data
public class AppUserWalletTransactionItemVO {

    @Schema(description = "流水记录ID")
    private Long id;

    @Schema(description = "收益标题")
    private String title;

    @Schema(description = "发生时间（yyyy-MM-dd HH:mm）")
    private String time;

    @Schema(description = "收益金额（元，保留两位小数）")
    private String amount;

    @Schema(description = "结算状态文案：已到账 / 待结算")
    private String status;
}
