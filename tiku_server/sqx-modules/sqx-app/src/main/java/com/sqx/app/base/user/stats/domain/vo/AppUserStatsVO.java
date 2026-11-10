package com.sqx.app.base.user.stats.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端学习统计出参（GET /app/user/stats）
 */
@Data
public class AppUserStatsVO {

    @Schema(description = "累计学习天数（有练习提交记录的自然日去重计数）")
    private Integer studyDays;

    @Schema(description = "累计做题总数（练习记录 total_count 求和）")
    private Integer questionCount;

    @Schema(description = "已获得勋章数量（基于练习记录里程碑规则计算）")
    private Integer badgeCount;
}
