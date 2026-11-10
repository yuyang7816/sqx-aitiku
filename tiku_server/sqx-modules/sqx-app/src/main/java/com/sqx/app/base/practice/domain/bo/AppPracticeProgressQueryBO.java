package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端章节练习进度查询入参（GET /app/practice/progress）
 */
@Data
public class AppPracticeProgressQueryBO {

    @Schema(description = "章业务编码（chapter_code，如 rational-numbers）")
    @NotBlank(message = "chapterId 不能为空")
    private String chapterId;
}
