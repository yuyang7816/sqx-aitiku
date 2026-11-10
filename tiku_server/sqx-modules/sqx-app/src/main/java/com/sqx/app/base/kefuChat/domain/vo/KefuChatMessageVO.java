package com.sqx.app.base.kefuChat.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * IM 消息项
 */
@Data
public class KefuChatMessageVO {

    @Schema(description = "消息 ID")
    private Long id;

    @Schema(description = "发送方：user / staff")
    private String senderRole;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "当前用户视角是否已读")
    private Boolean read;

    @Schema(description = "发送时间，如 2026-08-13 10:24")
    private String time;
}
