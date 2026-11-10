package com.sqx.app.base.question.paper.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 题集绑定题目分页查询
 */
@Data
public class QuestionPaperItemPageBo {

    @Schema(description = "题集 id")
    @NotNull(message = "题集 id 不能为空")
    private Long paperId;

    @Schema(description = "页码")
    private Long page = 1L;

    @Schema(description = "每页条数")
    private Long limit = 10L;

    @Schema(description = "题目编码关键词")
    private String questionCode;

    @Schema(description = "题干关键词")
    private String title;
}
