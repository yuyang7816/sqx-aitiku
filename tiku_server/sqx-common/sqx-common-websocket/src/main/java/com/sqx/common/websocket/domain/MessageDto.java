package com.sqx.common.websocket.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chengming
 * @since 2024/12/31
 */
@Data
public class MessageDto {
    /**
     * 目标对象id
     */
    @NotNull(message = "目标对象编号不能为空")
    private String targetId;

    /**
     * 消息类型 text: 文本 binary: 二进制
     */
    private String messageType;

    /**
     * 消息目标类型 1用户 2群组
     */
    private Integer targetType;

    /**
     * 消息内容（可以使JSON字符串）
     */
    @NotNull(message = "消息内容不能为空")
    private String content;
}
