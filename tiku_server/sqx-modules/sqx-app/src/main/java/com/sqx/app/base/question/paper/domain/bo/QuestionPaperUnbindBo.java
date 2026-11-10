package com.sqx.app.base.question.paper.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 题集解绑题目
 */
@Data
public class QuestionPaperUnbindBo {

    @Schema(description = "题集 id")
    @NotNull(message = "题集 id 不能为空")
    private Long paperId;

    @Schema(description = "题目 id（推荐）")
    private Long questionId;

    @Schema(description = "题目业务编码（兼容旧接口）")
    private String questionCode;
}
