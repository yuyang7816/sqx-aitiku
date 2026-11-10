package com.sqx.app.base.ocr.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * OCR 次数配额快照（搜题扣减后返回，供 uniapp 同步 ocrDailyLimit/ocrUsedToday）
 */
@Data
public class AppOcrQuotaVO {

    @Schema(description = "每日 OCR 搜题次数上限（非会员有效；会员仍返回展示值）")
    private Integer ocrDailyLimit;

    @Schema(description = "今日已用 OCR 搜题次数（本次请求扣减后）")
    private Integer ocrUsedToday;

    @Schema(description = "是否会员无限次（true 时不校验上限）")
    private Boolean ocrUnlimited;
}
