package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识点
 */
@Getter
@Setter
@TableName("tb_knowledge_point")
@Schema(name = "KnowledgePoint", description = "知识点")
public class KnowledgePoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("point_code")
    private String pointCode;

    @TableField("title")
    private String title;

    @TableField("subject")
    private String subject;

    @TableField("grade")
    private String grade;

    @TableField("grade_short")
    private String gradeShort;

    @TableField("question_count")
    private Integer questionCount;

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
