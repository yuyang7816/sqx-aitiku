package com.sqx.app.base.user.stats.service;

import com.sqx.app.base.user.stats.domain.vo.AppUserStatsVO;

/**
 * 用户端学习统计（GET /app/user/stats）
 */
public interface UserStatsService {

    /**
     * 获取当前用户累计学习统计
     */
    AppUserStatsVO getStats(Long userId);
}
