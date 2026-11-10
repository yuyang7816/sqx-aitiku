package com.sqx.app.base.user.badge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserBadgeDefinitionVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "勋章业务编码")
    private String badgeCode;

    @Schema(description = "勋章名称")
    private String name;

    @Schema(description = "获得条件说明")
    private String conditionText;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "解锁目标值")
    private Integer targetValue;

    @Schema(description = "排序，升序")
    private Integer sortOrder;

    @Schema(description = "1=启用 0=禁用")
    private Integer state;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}
