package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户搜题记录
 */
@Getter
@Setter
@TableName("tb_user_search_record")
@Schema(name = "UserSearchRecord", description = "用户搜题记录")
public class UserSearchRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户 id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "题干预览")
    @TableField("title")
    private String title;

    @Schema(description = "来源：拍照搜题/手动搜题/AI答疑")
    @TableField("source")
    private String source;

    @Schema(description = "原图地址（拍照搜题可选）")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "首次搜题时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "最近更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
