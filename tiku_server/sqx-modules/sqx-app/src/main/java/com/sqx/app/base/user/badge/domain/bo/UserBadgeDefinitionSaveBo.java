package com.sqx.app.base.user.badge.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserBadgeDefinitionSaveBo {

    @NotBlank(message = "勋章编码不能为空")
    @Schema(description = "勋章业务编码")
    private String badgeCode;

    @NotBlank(message = "勋章名称不能为空")
    @Schema(description = "勋章名称")
    private String name;

    @NotBlank(message = "获得条件说明不能为空")
    @Schema(description = "获得条件说明")
    private String conditionText;

    @NotBlank(message = "规则类型不能为空")
    @Schema(description = "submit_count/study_days/question_count/perfect_score")
    private String ruleType;

    @NotNull(message = "目标值不能为空")
    @Schema(description = "解锁目标值")
    private Integer targetValue;

    @Schema(description = "排序，升序")
    private Integer sortOrder;

    @Schema(description = "1=启用 0=禁用")
    private Integer state;
}
