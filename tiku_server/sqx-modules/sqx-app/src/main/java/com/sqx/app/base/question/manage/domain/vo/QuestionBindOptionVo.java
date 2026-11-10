package com.sqx.app.base.question.manage.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目表单绑定目标下拉项
 */
@Data
public class QuestionBindOptionVo {

    @Schema(description = "展示文案")
    private String label;

    @Schema(description = "提交值（pointCode / paperCode / chapterCode）")
    private String value;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "副标题")
    private String meta;
}
