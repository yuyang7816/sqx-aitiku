package com.sqx.app.base.ocr.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OCR 搜题入参（POST /app/ocr/search）
 * imageUrl 与 text 至少传一项；同时传时优先使用 text
 */
@Data
public class AppOcrSearchBO {

    @Schema(description = "已上传图片访问 URL（与 text 二选一）")
    private String imageUrl;

    @Schema(description = "题干文本或关键词（与 imageUrl 二选一，优先于图片 OCR）")
    private String text;
}
