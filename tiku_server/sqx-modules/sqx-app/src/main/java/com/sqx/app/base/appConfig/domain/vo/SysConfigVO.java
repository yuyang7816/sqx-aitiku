package com.sqx.app.base.appConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Data
public class SysConfigVO {

    @Schema(description = "配置")
    private String value;
}
