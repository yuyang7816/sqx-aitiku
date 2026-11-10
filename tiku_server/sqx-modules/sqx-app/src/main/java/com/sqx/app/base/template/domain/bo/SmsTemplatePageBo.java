package com.sqx.app.base.template.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SmsTemplatePageBo extends PageParam {

    @Schema(description = "业务标识 例如: login register")
    private String bizCode;

    @Schema(description = "模板内容")
    private String content;
}
