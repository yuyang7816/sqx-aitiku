package com.sqx.app.base.poster.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.service.AnalysisReportService;
import com.sqx.app.base.poster.domain.vo.AppReportPosterVO;
import com.sqx.app.base.poster.service.ReportPosterService;
import com.sqx.app.base.poster.support.ReportMiniProgramQrSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.YearMonth;

/**
 * 月度报告海报：聚合学情月报 + 小程序码
 */
@Slf4j
@Service
public class ReportPosterServiceImpl implements ReportPosterService {

    @Resource
    private AnalysisReportService analysisReportService;

    @Resource
    private ReportMiniProgramQrSupport reportMiniProgramQrSupport;

    @Override
    public AppReportPosterVO getReportPoster(Long userId, YearMonth month) {
        YearMonth target = month == null ? YearMonth.now() : month;
        AppAnalysisReportVO report = analysisReportService.getReport(userId, target);

        AppReportPosterVO vo = new AppReportPosterVO();
        vo.setMonth(StringUtils.defaultIfBlank(report.getMonth(), target.toString()));
        vo.setMonthTitle(report.getMonthTitle());
        vo.setRankTitle(report.getRankTitle());
        vo.setRankSubtitle(report.getRankSubtitle());
        vo.setMonthQuestionCount(report.getMonthQuestionCount());
        vo.setCorrectRate(report.getCorrectRate());
        vo.setRankPercent(report.getRankPercent());

        try {
            vo.setQrCodeUrl(reportMiniProgramQrSupport.buildReportQrCodeUrl(target));
        } catch (Exception ex) {
            log.warn("报告海报小程序码生成失败 userId={} month={}", userId, target, ex);
            vo.setQrCodeUrl("");
        }
        return vo;
    }
}
