package com.sqx.app.referral.commission.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReferralCommissionPageBo extends PageParam {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "订单id")
    private Long orderId;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "方向")
    private Integer direction;
}
