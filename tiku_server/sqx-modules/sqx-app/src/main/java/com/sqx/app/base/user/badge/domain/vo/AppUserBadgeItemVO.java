package com.sqx.app.base.user.badge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端勋章列表项（GET /app/user/badges 出参）
 */
@Data
@Schema(description = "用户勋章列表项")
public class AppUserBadgeItemVO {

    @Schema(description = "勋章业务编码（稳定标识）")
    private String badgeCode;

    @Schema(description = "勋章名称")
    private String name;

    @Schema(description = "获得条件说明")
    private String conditionText;

    @Schema(description = "是否已解锁")
    private Boolean unlocked;

    @Schema(description = "当前进度值（与 targetValue 同维度）")
    private Integer progressValue;

    @Schema(description = "解锁目标值")
    private Integer targetValue;
}
