package com.sqx.app.base.analysis.service;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisOverviewVO;

/**
 * 学情概览服务
 */
public interface AnalysisOverviewService {

    /**
     * 获取当前用户学情概览（本周题量、正确率、雷达维度、薄弱点 Top3）
     */
    AppAnalysisOverviewVO getOverview(Long userId);
}
