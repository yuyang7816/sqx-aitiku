package com.sqx.app.base.question.paper.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 题集批量绑定题目
 */
@Data
public class QuestionPaperBindBo {

    @Schema(description = "题集 id")
    @NotNull(message = "题集 id 不能为空")
    private Long paperId;

    @Schema(description = "题目 id 列表（推荐）")
    private List<Long> questionIds;

    @Schema(description = "题目业务编码列表（兼容旧接口）")
    private List<String> questionCodes;
}
