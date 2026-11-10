package com.sqx.app.referral.withdraw.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POST /app/withdrawRecord 提现申请出参
 */
@Data
public class AppWithdrawApplyResultVO {

    @Schema(description = "提现记录ID")
    private Long recordId;

    @Schema(description = "申请金额（元）")
    private BigDecimal amount;

    @Schema(description = "实际到账金额（元，已扣手续费）")
    private BigDecimal actualAmount;

    @Schema(description = "手续费（元）")
    private BigDecimal serviceFee;

    @Schema(description = "提现渠道：1=微信零钱 2=支付宝")
    private Integer accountType;

    @Schema(description = "提现渠道名称")
    private String accountTypeName;

    @Schema(description = "状态：1=待审核 2=打款成功 3=已拒绝")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
