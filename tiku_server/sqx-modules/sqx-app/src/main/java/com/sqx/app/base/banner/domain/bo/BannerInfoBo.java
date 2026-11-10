package com.sqx.app.base.banner.domain.bo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

/**
 * @author sqx
 * @since 2025-05-16 13:49:04
 */
@Data
public class BannerInfoBo {
    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

}
