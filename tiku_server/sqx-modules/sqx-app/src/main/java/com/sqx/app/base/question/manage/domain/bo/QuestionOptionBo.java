package com.sqx.app.base.question.manage.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理端题目选项
 */
@Data
public class QuestionOptionBo {

    @Schema(description = "选项标签（如 A、B）")
    @NotBlank(message = "选项标签不能为空")
    private String label;

    @Schema(description = "选项内容")
    @NotBlank(message = "选项内容不能为空")
    private String value;
}
