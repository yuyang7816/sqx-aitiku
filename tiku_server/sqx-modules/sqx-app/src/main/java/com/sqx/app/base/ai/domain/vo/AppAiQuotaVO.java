package com.sqx.app.base.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 次数配额快照（对话扣减后返回，供 uniapp 同步 aiDailyLimit/aiUsedToday）
 */
@Data
public class AppAiQuotaVO {

    @Schema(description = "每日 AI 答疑次数上限（非会员有效；会员仍返回展示值）")
    private Integer aiDailyLimit;

    @Schema(description = "今日已用 AI 答疑次数（本次请求扣减后）")
    private Integer aiUsedToday;

    @Schema(description = "是否会员无限次（true 时不校验上限）")
    private Boolean aiUnlimited;
}
