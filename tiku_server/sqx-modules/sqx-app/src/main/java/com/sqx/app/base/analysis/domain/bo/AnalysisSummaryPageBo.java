package com.sqx.app.base.analysis.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端用户学情汇总分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AnalysisSummaryPageBo extends PageParam {

    @Schema(description = "用户名（模糊）")
    private String userName;

    @Schema(description = "手机号（模糊）")
    private String phone;

    @Schema(description = "本周做题数下限（含）")
    private Integer minWeekQuestionCount;

    @Schema(description = "本周平均正确率下限 0~100（含）")
    private Integer minCorrectRate;
}
