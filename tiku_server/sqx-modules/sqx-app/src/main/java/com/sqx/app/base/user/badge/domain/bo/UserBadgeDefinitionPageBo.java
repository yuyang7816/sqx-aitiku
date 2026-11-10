package com.sqx.app.base.user.badge.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserBadgeDefinitionPageBo extends PageParam {

    @Schema(description = "勋章名称关键词")
    private String name;

    @Schema(description = "1=启用 0=禁用")
    private Integer state;
}
