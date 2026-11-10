package com.sqx.app.base.question.paper.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 题集内题目（GET /app/question/paper/{id}/questions 出参，供答题页直接使用）
 */
@Data
public class AppQuestionPaperQuestionVO {

    @Schema(description = "展示序号（如 01、02）")
    private String no;

    @Schema(description = "题目主键")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题型")
    private String type;

    @Schema(description = "难度")
    private String difficulty;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "正确答案")
    private String rightAnswer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "选项列表")
    private List<AppQuestionOptionVO> options;
}
