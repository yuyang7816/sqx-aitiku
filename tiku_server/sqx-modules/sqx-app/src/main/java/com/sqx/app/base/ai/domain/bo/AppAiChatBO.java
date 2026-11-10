package com.sqx.app.base.ai.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI 答疑对话入参（POST /app/ai/chat）
 */
@Data
public class AppAiChatBO {

    @Schema(description = "题目上下文（题干摘要或题目标题，供 AI 结合答疑）")
    private String topic;

    @Schema(description = "当前用户消息（首条追问或多轮追问正文，必填）")
    private String message;

    @Schema(description = "历史对话（role+text，多轮时按时间顺序传入）")
    private List<AppAiChatHistoryItemBO> history;
}
