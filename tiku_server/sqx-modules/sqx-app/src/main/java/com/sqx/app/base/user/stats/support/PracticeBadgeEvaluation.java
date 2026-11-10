package com.sqx.app.base.user.stats.support;

import lombok.Data;

/**
 * 单条练习勋章判定结果（供 GET /app/user/badges 映射出参）
 */
@Data
public class PracticeBadgeEvaluation {

    /** 勋章业务编码（稳定标识，非自增 ID） */
    private String badgeCode;

    /** 勋章名称 */
    private String name;

    /** 获得条件说明 */
    private String conditionText;

    /** 是否已解锁 */
    private boolean unlocked;

    /** 当前进度值（与 targetValue 同维度，便于前端展示进度） */
    private Integer progressValue;

    /** 解锁目标值 */
    private Integer targetValue;
}
