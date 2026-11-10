package com.sqx.app.base.user.badge.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserBadgeDefinitionInfoBo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键")
    private Long id;
}
