package com.sqx.app.base.kefuChat.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发送 IM 消息
 */
@Data
public class KefuChatSendBO {

    @Schema(description = "消息正文")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不超过 500 字")
    private String content;
}
