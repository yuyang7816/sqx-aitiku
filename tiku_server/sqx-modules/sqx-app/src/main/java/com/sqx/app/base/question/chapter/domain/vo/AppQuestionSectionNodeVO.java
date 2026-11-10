package com.sqx.app.base.question.chapter.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端章节树-节节点（code/title/sort/questionCount）
 */
@Schema(description = "章节树-节节点")
@Data
public class AppQuestionSectionNodeVO {

    @Schema(description = "节编码")
    private String code;

    @Schema(description = "节标题")
    private String title;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "题目数量")
    private Integer questionCount;
}
