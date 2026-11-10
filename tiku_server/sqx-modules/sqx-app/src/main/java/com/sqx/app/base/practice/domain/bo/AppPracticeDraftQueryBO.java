package com.sqx.app.base.practice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端练习答题暂存查询入参（GET /app/practice/draft）
 */
@Data
public class AppPracticeDraftQueryBO {

    @Schema(description = "章业务编码（chapter_code），与 paperId 至少填一项")
    private String chapterId;

    @Schema(description = "题集业务编码（paper_code）")
    private String paperId;

    @Schema(description = "单题预览时的题目业务编码（sourceMode=single 时使用）")
    private String questionId;

    @Schema(description = "练习来源：all/wrong/single，缺省 all")
    private String sourceMode;
}
