package com.sqx.app.base.question.knowledge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端知识点列表项
 */
@Schema(description = "知识点列表项（code 对应 uniapp practiceSource.knowledgePoint）")
@Data
public class AppQuestionKnowledgePointVO {

    @Schema(description = "知识点业务编码")
    private String code;

    @Schema(description = "知识点名称")
    private String title;

    @Schema(description = "展示副标题（如 共 3 题）")
    private String meta;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "年级简称")
    private String gradeShort;

    @Schema(description = "关联题目数")
    private Integer questionCount;
}
