package com.sqx.app.base.question.manage.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 题目表单：知识点/题集下拉查询
 */
@Data
public class QuestionBindOptionQueryBo {

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "题集类型：special / exam（题集下拉时使用）")
    private String paperType;

    @Schema(description = "标题关键字")
    private String keyword;
}
