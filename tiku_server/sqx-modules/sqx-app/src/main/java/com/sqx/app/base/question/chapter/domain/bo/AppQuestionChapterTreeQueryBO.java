package com.sqx.app.base.question.chapter.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端章节树查询入参（GET /app/question/chapter/tree）
 */
@Schema(description = "章节树查询入参（grade/subject/version 均为 ASCII 编码，禁止传中文）")
@Data
public class AppQuestionChapterTreeQueryBO {

    @Schema(description = "年级编码（如 junior_3）")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @Schema(description = "学科编码（如 math）")
    @NotBlank(message = "学科不能为空")
    private String subject;

    @Schema(description = "教材版本编码（如 renjiao）")
    @NotBlank(message = "教材版本不能为空")
    private String version;
}
