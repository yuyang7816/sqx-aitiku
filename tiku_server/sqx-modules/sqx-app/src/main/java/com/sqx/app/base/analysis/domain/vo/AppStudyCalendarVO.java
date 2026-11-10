package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习日历出参（GET /app/analysis/calendar）
 */
@Data
public class AppStudyCalendarVO {

    @Schema(description = "月份 yyyy-MM")
    private String month;

    @Schema(description = "当月有学习/打卡记录的日期 yyyy-MM-dd")
    private List<String> activeDates = new ArrayList<>();

    @Schema(description = "连续打卡天数（截至今日）")
    private Integer streakDays;

    @Schema(description = "今日是否已打卡/有练习")
    private Boolean todayChecked;

    @Schema(description = "当月累计学习天数")
    private Integer monthStudyDays;
}
