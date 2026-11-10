package com.sqx.app.referral.commission.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
@Data
public class ReferralCommissionUpdateBo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "订单id")
    private Long orderId;

    @Schema(description = "佣金比例")
    private BigDecimal rate;

    @Schema(description = "佣金金额")
    private BigDecimal amount;
}
