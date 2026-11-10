package com.sqx.app.base.user.badge.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserBadgeDefinitionUpdateBo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "勋章名称")
    private String name;

    @Schema(description = "获得条件说明")
    private String conditionText;

    @Schema(description = "submit_count/study_days/question_count/perfect_score")
    private String ruleType;

    @Schema(description = "解锁目标值")
    private Integer targetValue;

    @Schema(description = "排序，升序")
    private Integer sortOrder;

    @Schema(description = "1=启用 0=禁用")
    private Integer state;
}
