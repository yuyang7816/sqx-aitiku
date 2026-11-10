package com.sqx.app.base.messages.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端消息列表项（GET /app/messages 出参，与 uniapp notice.vue mock 字段兼容）
 */
@Data
public class AppMessageItemVO {

    @Schema(description = "消息主键（数据库 ID，用于后续标记已读等操作）")
    private Long messageId;

    @Schema(description = "跳转标识（如 notice-review、notice-member，对应 notice.vue 路由分支）")
    private String id;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "展示用相对时间（如刚刚、今天 09:30）")
    private String time;

    @Schema(description = "是否已读")
    private Boolean read;

    @Schema(description = "消息类型: 1=系统公告, 2=用户消息")
    private Integer type;

    @Schema(description = "关联业务数据 ID（如订单 ID、报告 ID）")
    private Long dataId;

    @Schema(description = "图片 URL，逗号分隔")
    private String imageUrls;

    @Schema(description = "原始跳转链接或路由标识")
    private String url;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
