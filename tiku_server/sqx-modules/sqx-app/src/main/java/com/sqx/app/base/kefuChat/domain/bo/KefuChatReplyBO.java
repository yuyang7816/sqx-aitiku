package com.sqx.app.base.kefuChat.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理端回复 IM 消息
 */
@Data
public class KefuChatReplyBO {

    @Schema(description = "用户 ID")
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    @Schema(description = "回复正文")
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不超过 500 字")
    private String content;
}
