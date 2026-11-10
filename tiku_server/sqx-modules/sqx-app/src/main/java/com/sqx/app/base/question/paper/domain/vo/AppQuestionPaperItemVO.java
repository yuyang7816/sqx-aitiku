package com.sqx.app.base.question.paper.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题集列表项（GET /app/question/paper/list 出参）
 */
@Data
public class AppQuestionPaperItemVO {

    @Schema(description = "题集业务编码（paperId）")
    private String paperId;

    @Schema(description = "题集标题")
    private String title;

    @Schema(description = "类型：special / exam")
    private String type;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "年级简称")
    private String gradeShort;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "题目数量")
    private Integer questionCount;
}
