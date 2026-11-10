package com.sqx.app.base.setup.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教材版本选项（与 uniapp plan.vue setupVersions 项结构一致）
 */
@Data
public class SetupVersionOptionVO {

    @Schema(description = "版本展示名，如：人教版")
    private String label;

    @Schema(description = "版本值，写入 profile.version")
    private String value;
}
