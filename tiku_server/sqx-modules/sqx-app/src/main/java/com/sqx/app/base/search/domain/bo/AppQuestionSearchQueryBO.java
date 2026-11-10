package com.sqx.app.base.search.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 题库关键词搜索入参（GET /app/search/questions）
 */
@Data
public class AppQuestionSearchQueryBO {

    @NotBlank(message = "keyword 不能为空")
    @Size(max = 100, message = "keyword 最多 100 字")
    @Schema(description = "搜索关键词（题干/知识点）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数，最大 20", example = "10")
    private Integer size = 10;
}
