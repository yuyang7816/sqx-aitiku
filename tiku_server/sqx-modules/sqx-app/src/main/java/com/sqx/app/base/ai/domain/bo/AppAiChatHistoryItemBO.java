package com.sqx.app.base.ai.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 对话历史单条（POST /app/ai/chat 入参 history 元素）
 */
@Data
public class AppAiChatHistoryItemBO {

    @Schema(description = "角色：user 用户 / ai 或 assistant AI 助手")
    private String role;

    @Schema(description = "消息正文（兼容 text/content/message 三字段，服务端优先 text）")
    private String text;

    @Schema(description = "消息正文别名 content")
    private String content;

    @Schema(description = "消息正文别名 message")
    private String message;
}
