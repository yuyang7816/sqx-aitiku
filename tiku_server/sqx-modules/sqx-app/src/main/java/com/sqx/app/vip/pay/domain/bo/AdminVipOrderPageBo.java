package com.sqx.app.vip.pay.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminVipOrderPageBo extends PageParam {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "订单号（模糊）")
    private String orderNo;

    @Schema(description = "支付状态 0待支付 1已支付 2已关闭")
    private Integer payStatus;
}
