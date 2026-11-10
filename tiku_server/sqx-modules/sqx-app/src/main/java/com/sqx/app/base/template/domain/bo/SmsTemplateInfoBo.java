package com.sqx.app.base.template.domain.bo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

/**
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@Data
public class SmsTemplateInfoBo {
    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

}
