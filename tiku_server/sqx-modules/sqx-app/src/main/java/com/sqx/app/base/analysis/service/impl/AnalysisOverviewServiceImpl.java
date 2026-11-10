package com.sqx.app.base.analysis.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisOverviewVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisRadarDimensionVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisWeakPointVO;
import com.sqx.app.base.analysis.service.AnalysisOverviewService;
import com.sqx.app.base.analysis.support.StudyOverviewRawStats;
import com.sqx.app.base.analysis.support.StudyOverviewStatsProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 学情概览：经 {@link StudyOverviewStatsProvider} 聚合练习记录
 */
@Service
public class AnalysisOverviewServiceImpl implements AnalysisOverviewService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int WEAK_POINT_LIMIT = 3;
    private static final int MIN_CHAPTER_QUESTIONS = 1;

    @Resource
    private StudyOverviewStatsProvider studyOverviewStatsProvider;

    @Override
    public AppAnalysisOverviewVO getOverview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        StudyOverviewRawStats raw = studyOverviewStatsProvider.aggregateOverview(userId, today);

        AppAnalysisOverviewVO vo = new AppAnalysisOverviewVO();
        vo.setWeekStartDate(weekStart.format(DATE_FORMAT));
        vo.setWeekEndDate(weekEnd.format(DATE_FORMAT));
        vo.setWeekQuestionCount(Math.max(0, raw.getWeekQuestionCount()));
        vo.setWeekQuestionChangePercent(calcRelativeChangePercent(
                raw.getWeekQuestionCount(), raw.getLastWeekQuestionCount()));
        vo.setCorrectRate(calcCorrectRate(raw.getWeekCorrectCount(), raw.getWeekQuestionCount()));
        vo.setCorrectRateChange(calcCorrectRate(raw.getWeekCorrectCount(), raw.getWeekQuestionCount())
                - calcCorrectRate(raw.getLastWeekCorrectCount(), raw.getLastWeekQuestionCount()));
        vo.setRadarDimensions(buildRadarDimensions(raw.getSubjectAggs()));
        vo.setWeakPoints(buildWeakPoints(raw.getChapterAggs()));
        return vo;
    }

    private int calcCorrectRate(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(correctCount * 100f / totalCount));
    }

    private int calcRelativeChangePercent(int current, int previous) {
        if (previous <= 0) {
            return current > 0 ? 100 : 0;
        }
        return Math.round((current - previous) * 100f / previous);
    }

    private List<AppAnalysisRadarDimensionVO> buildRadarDimensions(List<StudyOverviewRawStats.SubjectAgg> subjectAggs) {
        List<AppAnalysisRadarDimensionVO> dimensions = new ArrayList<>();
        if (subjectAggs == null) {
            return dimensions;
        }
        subjectAggs.stream()
                .sorted(Comparator.comparing(StudyOverviewRawStats.SubjectAgg::getSubject))
                .forEach(agg -> {
                    AppAnalysisRadarDimensionVO item = new AppAnalysisRadarDimensionVO();
                    item.setName(agg.getSubject());
                    item.setScore(calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount()));
                    dimensions.add(item);
                });
        return dimensions;
    }

    private List<AppAnalysisWeakPointVO> buildWeakPoints(List<StudyOverviewRawStats.ChapterAgg> chapterAggs) {
        List<AppAnalysisWeakPointVO> weakPoints = new ArrayList<>();
        if (chapterAggs == null) {
            return weakPoints;
        }
        chapterAggs.stream()
                .filter(agg -> agg.getTotalCount() >= MIN_CHAPTER_QUESTIONS)
                .sorted(Comparator
                        .comparingInt((StudyOverviewRawStats.ChapterAgg agg) ->
                                calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount()))
                        .thenComparing(Comparator.comparingInt(StudyOverviewRawStats.ChapterAgg::getTotalCount).reversed()))
                .limit(WEAK_POINT_LIMIT)
                .forEach(agg -> {
                    int correctRate = calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount());
                    AppAnalysisWeakPointVO item = new AppAnalysisWeakPointVO();
                    item.setTitle(agg.getChapterTitle());
                    item.setChapterCode(agg.getChapterCode());
                    item.setCorrectRate(correctRate);
                    item.setPercent(correctRate + "%");
                    item.setQuestionCount(agg.getTotalCount());
                    item.setAction(resolveWeakAction(agg));
                    weakPoints.add(item);
                });
        return weakPoints;
    }

    private String resolveWeakAction(StudyOverviewRawStats.ChapterAgg agg) {
        if (agg.getWrongCount() > 0 && agg.getWrongCount() >= agg.getCorrectCount()) {
            return "wrong-practice";
        }
        String subject = StringUtils.defaultString(agg.getSubject());
        if (subject.contains("英语") || subject.contains("语文")) {
            return "analysis-chat";
        }
        return "ocr-practice";
    }
}
