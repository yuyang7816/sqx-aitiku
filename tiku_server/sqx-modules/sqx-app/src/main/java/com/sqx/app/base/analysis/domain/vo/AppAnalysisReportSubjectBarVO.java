package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 月度报告学科正确率柱状图项（GET /app/analysis/report.subjectBars）
 */
@Data
public class AppAnalysisReportSubjectBarVO {

    @Schema(description = "学科名称（如 数学）")
    private String label;

    @Schema(description = "该学科正确率 0~100")
    private Integer correctRate;

    @Schema(description = "柱状图高度展示文案（如 88%）")
    private String percent;
}
