package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户端月度学习报告出参（GET /app/analysis/report）
 */
@Data
public class AppAnalysisReportVO {

    @Schema(description = "报告月份（yyyy-MM）")
    private String month;

    @Schema(description = "报告标题（如 2026年6月 · 学习报告）")
    private String monthTitle;

    @Schema(description = "月起始日期（yyyy-MM-dd）")
    private String monthStartDate;

    @Schema(description = "月结束日期（yyyy-MM-dd）")
    private String monthEndDate;

    @Schema(description = "当月累计做题数量")
    private Integer monthQuestionCount;

    @Schema(description = "当月平均正确率 0~100")
    private Integer correctRate;

    @Schema(description = "较上月题量变化百分比（相对变化，如 12 表示 +12%）")
    private Integer monthQuestionChangePercent;

    @Schema(description = "排名主文案（如 超越了 88% 的同龄人）")
    private String rankTitle;

    @Schema(description = "排名副文案（如 你在数学和物理方面的进步非常显著）")
    private String rankSubtitle;

    @Schema(description = "超越同龄人百分比 0~100（与 peerComparison.beatPercent 一致）")
    private Integer rankPercent;

    @Schema(description = "同龄对比统计")
    private AppAnalysisPeerComparisonVO peerComparison;

    @Schema(description = "各学科正确率柱状图")
    private List<AppAnalysisReportSubjectBarVO> subjectBars = new ArrayList<>();

    @Schema(description = "AI 学习建议（通常 3 条）")
    private List<AppAnalysisReportAdviceVO> aiAdvices = new ArrayList<>();
}
