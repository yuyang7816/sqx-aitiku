package com.sqx.app.base.analysis.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisTodayVO;
import com.sqx.app.base.analysis.service.AnalysisTodayService;
import com.sqx.app.base.analysis.support.StudyTodayRawStats;
import com.sqx.app.base.analysis.support.StudyTodayStatsProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 今日学情统计：经 {@link StudyTodayStatsProvider} 聚合，当前为 Mock 实现。
 */
@Service
public class AnalysisTodayServiceImpl implements AnalysisTodayService {

    private static final DateTimeFormatter STAT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private StudyTodayStatsProvider studyTodayStatsProvider;

    @Override
    public AppAnalysisTodayVO getTodayStats(Long userId) {
        LocalDate today = LocalDate.now();
        StudyTodayRawStats raw = studyTodayStatsProvider.aggregateToday(userId, today);

        AppAnalysisTodayVO vo = new AppAnalysisTodayVO();
        vo.setStatDate(today.format(STAT_DATE_FORMAT));
        vo.setDurationMinutes(Math.max(0, raw.getDurationMinutes()));
        vo.setQuestionCount(Math.max(0, raw.getQuestionCount()));
        return vo;
    }
}
