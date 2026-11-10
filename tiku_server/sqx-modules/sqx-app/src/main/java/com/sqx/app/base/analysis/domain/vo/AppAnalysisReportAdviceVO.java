package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 月度报告 AI 学习建议项（GET /app/analysis/report.aiAdvices）
 */
@Data
public class AppAnalysisReportAdviceVO {

    @Schema(description = "建议序号（从 1 开始）")
    private Integer order;

    @Schema(description = "建议正文")
    private String content;

    @Schema(description = "推荐动作：wrong-practice / analysis-chat / ocr-practice / calendar-checkin")
    private String action;
}
