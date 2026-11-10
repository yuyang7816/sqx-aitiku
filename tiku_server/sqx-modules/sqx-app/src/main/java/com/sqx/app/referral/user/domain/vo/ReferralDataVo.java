package com.sqx.app.referral.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chengming
 * @since 2025/8/4
 */
@Data
public class ReferralDataVo {

    @Schema(description = "邀请人数")
    private Long num;

    @Schema(description = "佣金")
    private BigDecimal commission;
}
