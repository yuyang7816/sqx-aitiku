package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 月度报告同龄对比（GET /app/analysis/report.peerComparison）
 */
@Data
public class AppAnalysisPeerComparisonVO {

    @Schema(description = "年级展示文案（如 初三）")
    private String gradeLabel;

    @Schema(description = "超越同龄人百分比 0~100")
    private Integer beatPercent;

    @Schema(description = "参与对比的同龄样本人数")
    private Integer peerSampleCount;

    @Schema(description = "本人当月平均正确率 0~100")
    private Integer userCorrectRate;

    @Schema(description = "同龄当月平均正确率 0~100")
    private Integer peerAvgCorrectRate;
}
