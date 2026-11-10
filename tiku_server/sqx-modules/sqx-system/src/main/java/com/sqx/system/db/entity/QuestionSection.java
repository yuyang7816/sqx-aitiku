package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库-节
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_question_section")
@Schema(name = "QuestionSection", description = "题库-节")
public class QuestionSection implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "业务编码")
    @TableField("section_code")
    private String sectionCode;

    @Schema(description = "所属章 id")
    @TableField("chapter_id")
    private Long chapterId;

    @Schema(description = "节标题")
    @TableField("title")
    private String title;

    @Schema(description = "题目数量")
    @TableField("question_count")
    private Integer questionCount;

    @Schema(description = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "状态：1启用 2禁用")
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
