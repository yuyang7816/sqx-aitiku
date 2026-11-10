package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户练习提交记录
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_user_practice_record")
@Schema(name = "UserPracticeRecord", description = "用户练习提交记录")
public class UserPracticeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "练习记录业务编码")
    @TableField("record_code")
    private String recordCode;

    @Schema(description = "用户 id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "章业务编码")
    @TableField("chapter_code")
    private String chapterCode;

    @Schema(description = "章标题快照")
    @TableField("chapter_title")
    private String chapterTitle;

    @Schema(description = "练习来源：all/wrong/single")
    @TableField("source_mode")
    private String sourceMode;

    @Schema(description = "学科展示文案")
    @TableField("subject")
    private String subject;

    @Schema(description = "本次答题总数")
    @TableField("total_count")
    private Integer totalCount;

    @Schema(description = "答对题数")
    @TableField("correct_count")
    private Integer correctCount;

    @Schema(description = "答错题数")
    @TableField("wrong_count")
    private Integer wrongCount;

    @Schema(description = "得分 0~100")
    @TableField("score")
    private Integer score;

    @Schema(description = "答题耗时（秒）")
    @TableField("duration_seconds")
    private Integer durationSeconds;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
