package com.sqx.app.base.kefuChat.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客服在线 IM 消息
 */
@Getter
@Setter
@TableName("kefu_chat_message")
@Schema(name = "KefuChatMessage", description = "客服在线 IM 消息")
public class KefuChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "C 端用户 ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "发送方：user=用户 staff=客服")
    @TableField("sender_role")
    private String senderRole;

    @Schema(description = "消息正文")
    @TableField("content")
    private String content;

    @Schema(description = "接收方是否已读：0=未读 1=已读")
    @TableField("is_read")
    private Integer isRead;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
