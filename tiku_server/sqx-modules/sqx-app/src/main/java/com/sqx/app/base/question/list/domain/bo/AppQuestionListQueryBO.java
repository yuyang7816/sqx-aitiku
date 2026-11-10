package com.sqx.app.base.question.list.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端练习题目拉取入参（GET /app/question/list）
 * chapterId / paperId / questionIds 至少传一项
 */
@Schema(description = "题目列表查询入参（业务键 chapterId/paperId/questionIds，至少传一项）")
@Data
public class AppQuestionListQueryBO {

    @Schema(description = "章业务编码（chapter_code，如 rational-numbers）")
    private String chapterId;

    @Schema(description = "题集业务编码（paper_code，如 special-rational-numbers）")
    private String paperId;

    @Schema(description = "题目 id 或 question_code，逗号分隔（mode=single 时必填）")
    private String questionIds;

    @Schema(description = "知识点业务编码（point_code），与 chapterId/paperId/questionIds 四选一")
    private String knowledgePoint;

    @Schema(description = "拉取模式：all 全部 / wrong 仅错题 / single 指定题，默认 all")
    private String mode;

    @Schema(description = "页码，默认 1")
    private Long page;

    @Schema(description = "每页条数，默认 10")
    private Long size;
}
