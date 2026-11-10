package com.sqx.app.referral.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端推广收益概览（GET /app/referral/userCommission）
 */
@Data
public class AppUserCommissionSummaryVO {

    @Schema(description = "累计推广收益（订单佣金入账合计，元）")
    private BigDecimal totalCommission;

    @Schema(description = "今日推广收益（当日订单佣金入账合计，元）")
    private BigDecimal todayCommission;

    @Schema(description = "推广人数（直接下级用户数）")
    private Long inviteCount;
}
