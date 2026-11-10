package com.sqx.app.base.setup.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 年级选项（与 uniapp plan.vue setupGrades 项结构一致）
 */
@Data
public class SetupGradeOptionVO {

    @Schema(description = "年级简称，UI 展示用，如：初一")
    private String label;

    @Schema(description = "年级完整名称，写入 profile.grade，如：初中一年级")
    private String value;

    @Schema(description = "所属学段编码，与 gradeStages.value 对应，如：junior")
    private String stageCode;

    @Schema(description = "所属学段展示名，如：初中")
    private String stageName;
}
