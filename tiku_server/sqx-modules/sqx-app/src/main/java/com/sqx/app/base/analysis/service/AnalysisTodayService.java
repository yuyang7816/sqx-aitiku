package com.sqx.app.base.analysis.service;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisTodayVO;

/**
 * 用户端今日学情统计
 */
public interface AnalysisTodayService {

    /**
     * 获取当前用户今日练习时长与题量
     *
     * @param userId 登录用户 ID
     * @return 今日统计 VO
     */
    AppAnalysisTodayVO getTodayStats(Long userId);
}
