package com.sqx.app.base.analysis.service;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;

import java.time.YearMonth;

/**
 * 月度学习报告
 */
public interface AnalysisReportService {

    /**
     * 获取指定自然月的学习报告
     *
     * @param userId 用户 id
     * @param month  自然月
     * @return 月度报告 VO
     */
    AppAnalysisReportVO getReport(Long userId, YearMonth month);
}
