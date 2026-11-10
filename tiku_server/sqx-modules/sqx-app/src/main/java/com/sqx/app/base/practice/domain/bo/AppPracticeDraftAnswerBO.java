package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端练习暂存单题答案（POST /app/practice/draft 入参项）
 */
@Data
public class AppPracticeDraftAnswerBO {

    @Schema(description = "题目业务编码（question_code）")
    @NotBlank(message = "题目编码不能为空")
    private String questionCode;

    @Schema(description = "用户所选答案（如 A、B；可为空表示已定位但未作答）")
    private String userAnswer;
}
