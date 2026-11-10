package com.sqx.app.base.analysis.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端用户学情汇总详情
 */
@Data
public class AnalysisSummaryInfoBo {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "C 端用户 ID")
    private Long userId;
}
