package com.sqx.app.vip.pay.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AdminVipOrderInfoBo {

    @NotNull(message = "订单 ID 不能为空")
    @Schema(description = "订单 ID")
    private Long id;
}
