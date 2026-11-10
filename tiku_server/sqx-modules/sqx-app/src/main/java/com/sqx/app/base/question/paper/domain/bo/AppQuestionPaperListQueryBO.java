package com.sqx.app.base.question.paper.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 题集列表查询入参（GET /app/question/paper/list）
 */
@Data
public class AppQuestionPaperListQueryBO {

    @Schema(description = "题集类型：special专项训练 exam真题模拟")
    @NotBlank(message = "type 不能为空")
    @Pattern(regexp = "special|exam", message = "type 仅支持 special 或 exam")
    private String type;
}
