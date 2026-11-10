package com.sqx.app.base.question.paper.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端搜索可绑定题目
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionPaperQuestionSearchBo extends PageParam {

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题干关键词")
    private String title;
}
