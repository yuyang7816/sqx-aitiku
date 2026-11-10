package com.sqx.app.base.question.knowledge.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识点列表查询入参（GET /app/question/knowledge/list）
 */
@Schema(description = "知识点列表查询入参（grade/subject 可选筛选）")
@Data
public class AppQuestionKnowledgeListQueryBO {

    @Schema(description = "年级编码（如 junior_1，禁止传中文）")
    private String grade;

    @Schema(description = "学科编码（如 math，禁止传中文）")
    private String subject;
}
