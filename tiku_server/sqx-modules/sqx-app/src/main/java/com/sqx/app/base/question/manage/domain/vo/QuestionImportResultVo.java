package com.sqx.app.base.question.manage.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目批量导入结果
 */
@Data
public class QuestionImportResultVo {

    @Schema(description = "成功导入数")
    private Integer successCount = 0;

    @Schema(description = "跳过数（编码已存在且 skipExisting=true）")
    private Integer skipCount = 0;

    @Schema(description = "失败数")
    private Integer failCount = 0;

    @Schema(description = "失败明细")
    private List<QuestionImportErrorVo> errors = new ArrayList<>();
}
