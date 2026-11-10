package com.sqx.app.base.question.paper.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目选项（答题页展示）
 */
@Data
public class AppQuestionOptionVO {

    @Schema(description = "选项标签（如 A、B）")
    private String label;

    @Schema(description = "选项内容")
    private String value;
}
