package com.sqx.app.base.analysis.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.analysis.domain.bo.AnalysisReportLogPageBo;
import com.sqx.app.base.analysis.domain.bo.AnalysisSummaryInfoBo;
import com.sqx.app.base.analysis.domain.bo.AnalysisSummaryPageBo;
import com.sqx.app.base.analysis.domain.vo.AdminAnalysisReportLogVo;
import com.sqx.app.base.analysis.domain.vo.AdminAnalysisSummaryVo;

/**
 * 管理端学情统计查询
 */
public interface AdminAnalysisService {

    /**
     * 分页查询用户学情汇总（本周题量、正确率、今日练习、薄弱点摘要）
     */
    IPage<AdminAnalysisSummaryVo> pageSummary(AnalysisSummaryPageBo pageBo);

    /**
     * 学情汇总详情（含雷达维度与薄弱点 Top3）
     */
    AdminAnalysisSummaryVo summaryInfo(AnalysisSummaryInfoBo infoBo);

    /**
     * 分页查询月度报告生成/推送日志（messages.url=notice-report）
     */
    IPage<AdminAnalysisReportLogVo> pageReportLog(AnalysisReportLogPageBo pageBo);
}
