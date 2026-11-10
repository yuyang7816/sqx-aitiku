package com.sqx.app.base.user.stats.support;

/**
 * 用户学习统计聚合（基于 tb_user_practice_record）
 */
public interface UserStudyStatsProvider {

    /**
     * 聚合指定用户的累计学习统计
     */
    UserStudyRawStats aggregate(Long userId);
}
