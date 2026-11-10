package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学习打卡出参（POST /app/analysis/check-in）
 */
@Data
public class AppStudyCheckInVO {

    @Schema(description = "打卡日期 yyyy-MM-dd")
    private String checkDate;

    @Schema(description = "连续打卡天数")
    private Integer streakDays;

    @Schema(description = "是否新打卡（false 表示今日已打卡）")
    private Boolean newlyChecked;
}
