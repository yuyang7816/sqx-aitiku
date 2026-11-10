package com.sqx.app.base.poster.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 月度报告海报数据（GET /app/poster/report）
 */
@Data
public class AppReportPosterVO {

    @Schema(description = "月份 yyyy-MM")
    private String month;

    @Schema(description = "月份标题")
    private String monthTitle;

    @Schema(description = "排名主标题")
    private String rankTitle;

    @Schema(description = "排名副标题")
    private String rankSubtitle;

    @Schema(description = "当月刷题数")
    private Integer monthQuestionCount;

    @Schema(description = "正确率 0~100")
    private Integer correctRate;

    @Schema(description = "超越同龄人百分比")
    private Integer rankPercent;

    @Schema(description = "小程序码图片 URL（MP-WEIXIN 优先）")
    private String qrCodeUrl;
}
