package com.sqx.app.base.appConfig.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Data
public class SysConfigInfoBO {

    @Schema(description = "配置key")
    private String key;
}
