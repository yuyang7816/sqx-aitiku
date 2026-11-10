package com.sqx.app.referral.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2025/12/2
 */
@Data
public class ReferralLawyerVo {

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "昨日收益")
    private BigDecimal yesterdayEarn;
}
