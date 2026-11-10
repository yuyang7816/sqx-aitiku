package com.sqx.app.base.banner.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chengming
 * @since 2025/5/16
 */
@Data
public class BannerUpdateStateBo {

    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "状态: 1正常 2隐藏")
    private Integer state;
}
