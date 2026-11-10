package com.sqx.app.base.question.manage.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 管理端批量导入题目
 */
@Data
public class QuestionImportBo {

    @Schema(description = "待导入题目列表")
    @NotEmpty(message = "导入列表不能为空")
    @Valid
    private List<QuestionSaveBo> items;

    @Schema(description = "编码已存在时是否跳过（true=跳过，false=记为失败）")
    private Boolean skipExisting = true;
}
