package com.sqx.app.vip.plan.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:51:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VipPlanPageBo extends PageParam {

    @Schema(description = "套餐名称（模糊）")
    private String name;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;
}
