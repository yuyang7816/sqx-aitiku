package com.sqx.app.base.question.paper.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端可绑定题目选项
 */
@Data
public class QuestionPaperQuestionOptionVo {

    @Schema(description = "题目 id")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "题型")
    private String questionType;

    @Schema(description = "难度")
    private String difficulty;
}
