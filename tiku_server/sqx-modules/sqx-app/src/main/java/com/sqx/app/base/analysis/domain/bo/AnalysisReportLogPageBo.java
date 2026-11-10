package com.sqx.app.base.analysis.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理端月度报告生成日志分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AnalysisReportLogPageBo extends PageParam {

    @Schema(description = "报告月份关键字（如 2026年7月 或 2026-07）")
    private String reportMonth;

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "手机号（模糊）")
    private String phone;

    @Schema(description = "是否已读: 0=未读, 1=已读")
    private Integer isRead;

    @Schema(description = "推送起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "推送结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
