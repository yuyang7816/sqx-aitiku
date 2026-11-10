package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户错题本
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_user_question_wrong")
@Schema(name = "UserQuestionWrong", description = "用户错题本")
public class UserQuestionWrong implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 已掌握 */
    public static final int MASTERED_YES = 1;

    /** 未掌握 */
    public static final int MASTERED_NO = 0;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户 id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "题目 id")
    @TableField("question_id")
    private Long questionId;

    @Schema(description = "学科展示文案")
    @TableField("subject")
    private String subject;

    @Schema(description = "最近一次错误答案")
    @TableField("user_answer")
    private String userAnswer;

    @Schema(description = "累计做错次数")
    @TableField("wrong_count")
    private Integer wrongCount;

    @Schema(description = "是否已掌握：1是 0否")
    @TableField("mastered")
    private Integer mastered;

    @Schema(description = "最近练习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_practice_at")
    private LocalDateTime lastPracticeAt;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
