package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户答题记录
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_user_question_answer")
@Schema(name = "UserQuestionAnswer", description = "用户答题记录")
public class UserQuestionAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户 id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "题目 id")
    @TableField("question_id")
    private Long questionId;

    @Schema(description = "是否答对：1对 0错")
    @TableField("is_correct")
    private Integer isCorrect;

    @Schema(description = "用户所选答案")
    @TableField("user_answer")
    private String userAnswer;

    @Schema(description = "答题时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("answer_time")
    private LocalDateTime answerTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
