package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户端学情概览出参（GET /app/analysis/overview）
 */
@Data
public class AppAnalysisOverviewVO {

    @Schema(description = "本周起始日期（yyyy-MM-dd，周一）")
    private String weekStartDate;

    @Schema(description = "本周结束日期（yyyy-MM-dd，周日）")
    private String weekEndDate;

    @Schema(description = "本周累计做题数量")
    private Integer weekQuestionCount;

    @Schema(description = "较上周题量变化百分比（相对变化，如 12 表示 +12%）")
    private Integer weekQuestionChangePercent;

    @Schema(description = "本周平均正确率 0~100")
    private Integer correctRate;

    @Schema(description = "较上周正确率变化（百分点，如 -3 表示下降 3 个百分点）")
    private Integer correctRateChange;

    @Schema(description = "能力雷达维度（按学科聚合）")
    private List<AppAnalysisRadarDimensionVO> radarDimensions = new ArrayList<>();

    @Schema(description = "薄弱知识点 Top3（按章正确率升序）")
    private List<AppAnalysisWeakPointVO> weakPoints = new ArrayList<>();
}
