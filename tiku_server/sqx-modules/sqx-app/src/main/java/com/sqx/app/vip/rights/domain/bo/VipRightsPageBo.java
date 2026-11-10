package com.sqx.app.vip.rights.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VipRightsPageBo extends PageParam {

    @Schema(description = "权益名称（模糊）")
    private String name;

    @Schema(description = "权益业务键")
    private String rightsCode;
}
