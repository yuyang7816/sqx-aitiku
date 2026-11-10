package com.sqx.app.base.ocr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * OCR 搜题匹配题目项
 */
@Data
public class AppOcrMatchItemVO {

    @Schema(description = "题目主键")
    private Long questionId;

    @Schema(description = "题目业务编码")
    private String questionCode;

    @Schema(description = "题干")
    private String title;

    @Schema(description = "标准答案（对应题库 right_answer）")
    private String answer;

    @Schema(description = "解析")
    private String analysis;

    @Schema(description = "AI 通俗讲解要点")
    private List<String> tips;

    @Schema(description = "相似度 0~1，越大越相似")
    private Double similarity;
}
