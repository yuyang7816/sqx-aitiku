package com.sqx.app.base.search.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题库关键词搜索结果项（GET /app/search/questions）
 */
@Data
public class AppQuestionSearchItemVO {

    @Schema(description = "题目主键")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "所属章标题")
    private String chapterTitle;

    @Schema(description = "题型")
    private String questionType;

    @Schema(description = "难度")
    private String difficulty;
}
