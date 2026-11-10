package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库-题集题目关联
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_question_paper_item")
@Schema(name = "QuestionPaperItem", description = "题库-题集题目关联")
public class QuestionPaperItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "题集 id")
    @TableField("paper_id")
    private Long paperId;

    @Schema(description = "题目 id")
    @TableField("question_id")
    private Long questionId;

    @Schema(description = "题集内排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
