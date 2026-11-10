package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端练习提交单题答案（POST /app/practice/submit 入参项）
 */
@Data
public class AppPracticeSubmitAnswerBO {

    @Schema(description = "题目业务编码（question_code）")
    @NotBlank(message = "题目编码不能为空")
    private String questionCode;

    @Schema(description = "用户所选答案（如 A、B）")
    @NotBlank(message = "用户答案不能为空")
    private String userAnswer;
}
