package com.sqx.app.base.analysis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端今日学情统计出参（GET /app/analysis/today）
 */
@Data
public class AppAnalysisTodayVO {

    @Schema(description = "统计日期（yyyy-MM-dd，服务端本地日）")
    private String statDate;

    @Schema(description = "今日累计练习时长（分钟）")
    private Integer durationMinutes;

    @Schema(description = "今日已做题目数量")
    private Integer questionCount;
}
