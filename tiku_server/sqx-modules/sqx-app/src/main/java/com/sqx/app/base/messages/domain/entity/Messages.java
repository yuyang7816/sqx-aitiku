package com.sqx.app.base.messages.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息信息表
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Getter
@Setter
@TableName("messages")
@Schema(name = "Messages", description = "消息信息表")
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "消息ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "消息内容")
    @TableField("content")
    private String content;

    @Schema(description = "图片URL，逗号分隔")
    @TableField("image_urls")
    private String imageUrls;

    @Schema(description = "跳转链接")
    @TableField("url")
    private String url;

    @Schema(description = "消息类型: 1=系统公告, 2=用户消息")
    @TableField("type")
    private Integer type;

    @Schema(description = "接收用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "关联数据ID（如服务ID、订单ID等）")
    @TableField("data_id")
    private Long dataId;

    @Schema(description = "是否已读: 0=未读, 1=已读")
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
