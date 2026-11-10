package com.sqx.app.base.question.manage.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目导入失败行
 */
@Data
public class QuestionImportErrorVo {

    @Schema(description = "Excel/JSON 行号（从 1 开始）")
    private Integer rowNum;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "失败原因")
    private String message;

    public QuestionImportErrorVo(Integer rowNum, String questionCode, String message) {
        this.rowNum = rowNum;
        this.questionCode = questionCode;
        this.message = message;
    }
}
