package com.sqx.app.base.notebook.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户端错题本掌握态更新入参（PATCH /app/notebook）
 */
@Data
public class AppNotebookUpdateBO {

    @Schema(description = "题目 id 或 question_code")
    @NotBlank(message = "题目标识不能为空")
    private String questionId;

    @Schema(description = "是否已掌握：true 已掌握，false 待复习")
    @NotNull(message = "掌握状态不能为空")
    private Boolean mastered;
}
