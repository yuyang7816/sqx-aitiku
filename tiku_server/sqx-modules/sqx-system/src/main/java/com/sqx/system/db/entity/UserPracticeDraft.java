package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户练习答题暂存（未交卷）
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_user_practice_draft")
@Schema(name = "UserPracticeDraft", description = "用户练习答题暂存")
public class UserPracticeDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户 id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "练习范围键")
    @TableField("scope_key")
    private String scopeKey;

    @Schema(description = "章业务编码")
    @TableField("chapter_code")
    private String chapterCode;

    @Schema(description = "题集业务编码")
    @TableField("paper_code")
    private String paperCode;

    @Schema(description = "练习来源：all/wrong/single")
    @TableField("source_mode")
    private String sourceMode;

    @Schema(description = "学科展示文案")
    @TableField("subject")
    private String subject;

    @Schema(description = "当前题号索引（0 起）")
    @TableField("current_index")
    private Integer currentIndex;

    @Schema(description = "已累计答题耗时（秒）")
    @TableField("duration_seconds")
    private Integer durationSeconds;

    @Schema(description = "暂存答案 JSON")
    @TableField("answers_json")
    private String answersJson;

    @Schema(description = "最近暂存时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("saved_at")
    private LocalDateTime savedAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
