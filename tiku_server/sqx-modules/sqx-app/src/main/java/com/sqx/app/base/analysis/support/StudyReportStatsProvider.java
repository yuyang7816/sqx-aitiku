package com.sqx.app.base.analysis.support;

import java.time.YearMonth;

/**
 * 月度报告统计聚合（基于 tb_user_practice_record、tb_user_question_wrong、tb_user）
 */
public interface StudyReportStatsProvider {

    /**
     * 聚合指定用户某自然月的练习、同龄对比与错题复习数据
     *
     * @param userId 用户 id
     * @param month  自然月
     * @return 原始聚合结果
     */
    StudyReportRawStats aggregateReport(Long userId, YearMonth month);
}
