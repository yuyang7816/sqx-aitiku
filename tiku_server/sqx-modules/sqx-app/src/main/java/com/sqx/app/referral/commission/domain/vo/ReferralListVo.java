package com.sqx.app.referral.commission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

;

/**
 * @author chengming
 * @since 2025/8/4
 */
@Data
public class ReferralListVo {

    @Schema(description = ("用户名"))
    private String userName;

    @Schema(description = ("用户头像"))
    private String avatar;

    @Schema(description = ("金额"))
    private BigDecimal amount;
}
