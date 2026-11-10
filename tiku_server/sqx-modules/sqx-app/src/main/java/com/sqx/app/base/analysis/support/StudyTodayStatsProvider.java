package com.sqx.app.base.analysis.support;

import java.time.LocalDate;

/**
 * 今日练习统计聚合数据源（模块六练习提交接入后可替换为数据库实现）
 */
public interface StudyTodayStatsProvider {

    /**
     * 聚合指定用户在某自然日的练习时长与题量
     *
     * @param userId   用户 ID
     * @param statDate 统计日期（自然日）
     * @return 原始统计；无练习记录时 durationMinutes、questionCount 均为 0
     */
    StudyTodayRawStats aggregateToday(Long userId, LocalDate statDate);
}
