package com.sqx.app.base.kefuChat.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端 IM 会话摘要
 */
@Data
public class KefuChatSessionVO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "最后一条消息摘要")
    private String lastContent;

    @Schema(description = "最后消息时间")
    private String lastTime;

    @Schema(description = "客服未读条数（用户发送且未读）")
    private Integer unreadCount;
}
