package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 刷题中心练习入口卡片
 */
@Getter
@Setter
@TableName("practice_entry")
@Schema(name = "PracticeEntry", description = "刷题中心练习入口")
public class PracticeEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("subtitle")
    private String subtitle;

    @TableField("icon")
    private String icon;

    @TableField("icon_color")
    private String iconColor;

    @TableField("bg_style")
    private String bgStyle;

    @TableField("url")
    private String url;

    @TableField("entry_type")
    private String entryType;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("state")
    private Integer state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
