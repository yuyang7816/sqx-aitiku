package com.sqx.system.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识点-题目关联
 */
@Getter
@Setter
@TableName("tb_knowledge_point_question")
@Schema(name = "KnowledgePointQuestion", description = "知识点-题目关联")
public class KnowledgePointQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("point_id")
    private Long pointId;

    @TableField("question_id")
    private Long questionId;

    @TableField("sort_order")
    private Integer sortOrder;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
