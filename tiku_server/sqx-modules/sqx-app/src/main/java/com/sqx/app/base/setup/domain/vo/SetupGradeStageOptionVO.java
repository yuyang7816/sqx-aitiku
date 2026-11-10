package com.sqx.app.base.setup.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学段选项（小学 / 初中 / 高中）
 */
@Data
public class SetupGradeStageOptionVO {

    @Schema(description = "学段展示名，如：小学")
    private String label;

    @Schema(description = "学段编码，如：primary")
    private String value;
}
