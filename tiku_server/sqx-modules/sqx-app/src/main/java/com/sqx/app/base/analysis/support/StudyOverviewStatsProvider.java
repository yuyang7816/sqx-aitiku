package com.sqx.app.base.analysis.support;

import java.time.LocalDate;

/**
 * 学情概览统计 Provider（基于练习记录聚合）
 */
public interface StudyOverviewStatsProvider {

    /**
     * 聚合指定用户在某参考日所在自然周的学情概览数据
     *
     * @param userId  用户 id
     * @param refDate 参考日期（用于确定本周/上周区间）
     */
    StudyOverviewRawStats aggregateOverview(Long userId, LocalDate refDate);
}
