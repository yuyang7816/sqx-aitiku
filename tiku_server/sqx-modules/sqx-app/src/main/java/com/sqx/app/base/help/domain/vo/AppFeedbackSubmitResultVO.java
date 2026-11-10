package com.sqx.app.base.help.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 反馈提交结果（POST /app/feedback/submit）
 */
@Data
public class AppFeedbackSubmitResultVO {

    @Schema(description = "工单号")
    private String ticketNo;

    @Schema(description = "工单状态：pending/processing/replied")
    private String status;
}
