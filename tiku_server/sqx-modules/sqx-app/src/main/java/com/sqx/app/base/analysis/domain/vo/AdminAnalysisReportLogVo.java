package com.sqx.app.base.analysis.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端月度报告生成日志
 */
@Data
public class AdminAnalysisReportLogVo {

    @Schema(description = "消息 ID")
    private Long messageId;

    @Schema(description = "推送标题（含报告月份）")
    private String title;

    @Schema(description = "报告月份（从标题解析，如 2026-07）")
    private String reportMonth;

    @Schema(description = "推送内容摘要")
    private String content;

    @Schema(description = "C 端用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "是否已读: 0=未读, 1=已读")
    private Integer isRead;

    @Schema(description = "推送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
