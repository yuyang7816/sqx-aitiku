package com.sqx.app.base.collection.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端新增收藏入参（POST /app/collection）
 */
@Data
public class AppCollectionCreateBO {

    @Schema(description = "题目 id 或 question_code")
    @NotBlank(message = "题目标识不能为空")
    private String questionId;

    @Schema(description = "学科展示文案（如 数学 · 初三）")
    @NotBlank(message = "学科不能为空")
    private String subject;
}
