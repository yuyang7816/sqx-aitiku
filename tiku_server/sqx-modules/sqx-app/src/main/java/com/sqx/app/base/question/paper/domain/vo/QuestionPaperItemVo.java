package com.sqx.app.base.question.paper.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端题集绑定题目项
 */
@Data
public class QuestionPaperItemVo {

    @Schema(description = "关联项 id")
    private Long id;

    @Schema(description = "题目 id")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题干")
    private String questionTitle;

    @Schema(description = "题型")
    private String questionType;

    @Schema(description = "题集内排序")
    private Integer sortOrder;
}
