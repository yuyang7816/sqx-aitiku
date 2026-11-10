package com.sqx.app.base.template.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@Data
public class SmsTemplateSaveBo {
    @Schema(description = "业务标识 例如: login register")
    private String bizCode;

    @Schema(description = "模板内容")
    private String content;
}
