package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * banner
 *
 * @author sqx
 * @since 2025-05-16 13:49:04
 */
@Getter
@Setter
@TableName("banner")
@Schema(name = "Banner", description = "banner")
public class Banner implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "图片地址")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "分类1banner图 2金刚区分类")
    @TableField("classify")
    private Integer classify;

    @Schema(description = "跳转地址 ")
    @TableField("url")
    private String url;

    @Schema(description = "顺序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "描述")
    @TableField("description")
    private String description;

    @Schema(description = "状态1正常 2隐藏")
    @TableField("state")
    private Integer state;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
