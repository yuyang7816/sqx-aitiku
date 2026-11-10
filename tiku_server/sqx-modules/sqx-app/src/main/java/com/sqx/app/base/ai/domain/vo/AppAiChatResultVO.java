package com.sqx.app.base.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 答疑对话出参（POST /app/ai/chat）
 */
@Data
public class AppAiChatResultVO {

    @Schema(description = "AI 回复正文（uniapp 读取 data.reply）")
    private String reply;

    @Schema(description = "本次对话后的 AI 次数配额")
    private AppAiQuotaVO quota;
}
