package com.sqx.app.base.notebook.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端错题本写入入参（POST /app/notebook）
 */
@Data
public class AppNotebookCreateBO {

    @Schema(description = "题目 id 或 question_code")
    @NotBlank(message = "题目标识不能为空")
    private String questionId;

    @Schema(description = "学科展示文案（如 数学 · 初三）")
    @NotBlank(message = "学科不能为空")
    private String subject;

    @Schema(description = "用户错误答案（手动加入时可空）")
    private String userAnswer;
}
