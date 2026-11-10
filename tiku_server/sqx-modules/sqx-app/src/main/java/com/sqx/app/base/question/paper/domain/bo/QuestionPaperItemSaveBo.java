package com.sqx.app.base.question.paper.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端题集题目绑定项
 */
@Data
public class QuestionPaperItemSaveBo {

    @Schema(description = "题目业务编码")
    @NotBlank(message = "题目业务编码不能为空")
    private String questionCode;

    @Schema(description = "题集内排序")
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;
}
