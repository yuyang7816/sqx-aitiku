package com.sqx.app.base.ocr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * OCR 搜题出参（POST /app/ocr/search）
 */
@Data
public class AppOcrSearchResultVO {

    @Schema(description = "实际用于匹配的识别文本（文本入参或 OCR 结果）")
    private String recognizedText;

    @Schema(description = "匹配题目列表（按相似度降序）")
    private List<AppOcrMatchItemVO> matches;

    @Schema(description = "本次搜题后的 OCR 次数配额（ocrDailyLimit/ocrUsedToday）")
    private AppOcrQuotaVO quota;
}
