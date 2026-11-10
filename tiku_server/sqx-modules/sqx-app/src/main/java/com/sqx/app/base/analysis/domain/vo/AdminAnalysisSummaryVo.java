package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理端用户学情汇总列表项
 */
@Data
public class AdminAnalysisSummaryVo {

    @Schema(description = "C 端用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "本周起始日期（yyyy-MM-dd）")
    private String weekStartDate;

    @Schema(description = "本周结束日期（yyyy-MM-dd）")
    private String weekEndDate;

    @Schema(description = "本周累计做题数量")
    private Integer weekQuestionCount;

    @Schema(description = "较上周题量变化百分比")
    private Integer weekQuestionChangePercent;

    @Schema(description = "本周平均正确率 0~100")
    private Integer correctRate;

    @Schema(description = "较上周正确率变化（百分点）")
    private Integer correctRateChange;

    @Schema(description = "今日已做题目数量")
    private Integer todayQuestionCount;

    @Schema(description = "今日练习时长（分钟）")
    private Integer todayDurationMinutes;

    @Schema(description = "薄弱知识点摘要（Top3 标题，逗号分隔）")
    private String weakPointSummary;

    @Schema(description = "能力雷达维度")
    private List<AppAnalysisRadarDimensionVO> radarDimensions = new ArrayList<>();

    @Schema(description = "薄弱知识点 Top3")
    private List<AppAnalysisWeakPointVO> weakPoints = new ArrayList<>();
}
