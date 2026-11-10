package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学情概览薄弱知识点 Top3 条目
 */
@Data
public class AppAnalysisWeakPointVO {

    @Schema(description = "薄弱点展示标题（章标题快照）")
    private String title;

    @Schema(description = "章业务编码（chapter_code，供跳转练习）")
    private String chapterCode;

    @Schema(description = "正确率 0~100")
    private Integer correctRate;

    @Schema(description = "正确率展示文案（如 48%）")
    private String percent;

    @Schema(description = "该章累计答题数")
    private Integer questionCount;

    @Schema(description = "推荐强化动作：wrong-practice / ocr-practice / analysis-chat")
    private String action;
}
