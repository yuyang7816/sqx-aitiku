package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库-专项/真题题集
 *
 * @author sqx
 */
@Getter
@Setter
@TableName("tb_question_paper")
@Schema(name = "QuestionPaper", description = "题库-专项/真题题集")
public class QuestionPaper implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "题集业务编码")
    @TableField("paper_code")
    private String paperCode;

    @Schema(description = "题集标题")
    @TableField("title")
    private String title;

    @Schema(description = "类型：special专项 exam真题")
    @TableField("paper_type")
    private String paperType;

    @Schema(description = "简介")
    @TableField("description")
    private String description;

    @Schema(description = "年级")
    @TableField("grade")
    private String grade;

    @Schema(description = "年级简称")
    @TableField("grade_short")
    private String gradeShort;

    @Schema(description = "学科")
    @TableField("subject")
    private String subject;

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
