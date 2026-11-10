package com.sqx.app.base.template.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@Data
public class SmsTemplateUpdateBo {
    @Schema(description = "id")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "业务标识 例如: login register")
    private String bizCode;

    @Schema(description = "模板内容")
    private String content;
}
