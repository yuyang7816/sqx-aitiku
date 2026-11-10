package com.sqx.app.base.setup.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学科选项（与 uniapp plan.vue setupSubjects 项结构一致）
 */
@Data
public class SetupSubjectOptionVO {

    @Schema(description = "学科名称，写入 profile.subjects[]")
    private String name;

    @Schema(description = "uview 图标名，如 file-text")
    private String icon;

    @Schema(description = "适用学段编码列表：primary/junior/senior")
    private List<String> stageCodes;

    @Schema(description = "是否默认选中，服务端固定返回 false，由 uniapp 本地维护选中态")
    private Boolean active;
}
