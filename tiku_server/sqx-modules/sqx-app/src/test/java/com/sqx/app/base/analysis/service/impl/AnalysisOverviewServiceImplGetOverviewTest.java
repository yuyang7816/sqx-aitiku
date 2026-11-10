package com.sqx.app.base.analysis.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisOverviewVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisRadarDimensionVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisWeakPointVO;
import com.sqx.app.base.analysis.support.StudyOverviewRawStats;
import com.sqx.app.base.analysis.support.StudyOverviewStatsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/analysis/overview 学情概览单元测试
 */
@ExtendWith(MockitoExtension.class)
class AnalysisOverviewServiceImplGetOverviewTest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Mock
    private StudyOverviewStatsProvider studyOverviewStatsProvider;

    @InjectMocks
    private AnalysisOverviewServiceImpl analysisOverviewService;

    @Test
    void getOverview_whenNoPracticeData_shouldReturnZeroAndEmptyLists() {
        when(studyOverviewStatsProvider.aggregateOverview(eq(100L), any(LocalDate.class)))
                .thenReturn(new StudyOverviewRawStats());

        AppAnalysisOverviewVO result = analysisOverviewService.getOverview(100L);

        assertNotNull(result);
        assertEquals(expectedWeekStart(), result.getWeekStartDate());
        assertEquals(expectedWeekEnd(), result.getWeekEndDate());
        assertEquals(0, result.getWeekQuestionCount());
        assertEquals(0, result.getCorrectRate());
        assertTrue(result.getRadarDimensions().isEmpty());
        assertTrue(result.getWeakPoints().isEmpty());
        verify(studyOverviewStatsProvider).aggregateOverview(eq(100L), any(LocalDate.class));
    }

    @Test
    void getOverview_shouldMapAggregatedStatsToVo() {
        StudyOverviewRawStats raw = new StudyOverviewRawStats();
        raw.setWeekQuestionCount(20);
        raw.setWeekCorrectCount(16);
        raw.setLastWeekQuestionCount(10);
        raw.setLastWeekCorrectCount(8);

        StudyOverviewRawStats.SubjectAgg math = new StudyOverviewRawStats.SubjectAgg();
        math.setSubject("数学");
        math.setTotalCount(12);
        math.setCorrectCount(10);

        StudyOverviewRawStats.SubjectAgg english = new StudyOverviewRawStats.SubjectAgg();
        english.setSubject("英语");
        english.setTotalCount(8);
        english.setCorrectCount(4);
        raw.setSubjectAggs(Arrays.asList(math, english));

        StudyOverviewRawStats.ChapterAgg weakChapter = new StudyOverviewRawStats.ChapterAgg();
        weakChapter.setChapterCode("chapter-english-grammar");
        weakChapter.setChapterTitle("虚拟语气");
        weakChapter.setSubject("英语");
        weakChapter.setTotalCount(8);
        weakChapter.setCorrectCount(2);
        weakChapter.setWrongCount(6);
        raw.setChapterAggs(Arrays.asList(weakChapter));

        when(studyOverviewStatsProvider.aggregateOverview(eq(200L), any(LocalDate.class))).thenReturn(raw);

        AppAnalysisOverviewVO result = analysisOverviewService.getOverview(200L);

        assertEquals(20, result.getWeekQuestionCount());
        assertEquals(100, result.getWeekQuestionChangePercent());
        assertEquals(80, result.getCorrectRate());
        assertEquals(0, result.getCorrectRateChange());
        assertEquals(2, result.getRadarDimensions().size());
        assertEquals("数学", result.getRadarDimensions().get(0).getName());
        assertEquals(83, result.getRadarDimensions().get(0).getScore());

        assertEquals(1, result.getWeakPoints().size());
        AppAnalysisWeakPointVO weakPoint = result.getWeakPoints().get(0);
        assertEquals("虚拟语气", weakPoint.getTitle());
        assertEquals("chapter-english-grammar", weakPoint.getChapterCode());
        assertEquals(25, weakPoint.getCorrectRate());
        assertEquals("25%", weakPoint.getPercent());
        assertEquals("wrong-practice", weakPoint.getAction());
    }

    @Test
    void getOverview_shouldLimitWeakPointsToTop3SortedByCorrectRate() {
        StudyOverviewRawStats raw = new StudyOverviewRawStats();
        raw.setWeekQuestionCount(30);
        raw.setWeekCorrectCount(15);
        raw.setChapterAggs(Arrays.asList(
                buildChapter("chapter-a", "章节A", "数学", 10, 8, 2),
                buildChapter("chapter-b", "章节B", "数学", 10, 5, 5),
                buildChapter("chapter-c", "章节C", "数学", 10, 3, 7),
                buildChapter("chapter-d", "章节D", "数学", 10, 1, 9)
        ));
        when(studyOverviewStatsProvider.aggregateOverview(eq(300L), any(LocalDate.class))).thenReturn(raw);

        AppAnalysisOverviewVO result = analysisOverviewService.getOverview(300L);

        assertEquals(3, result.getWeakPoints().size());
        assertEquals("章节D", result.getWeakPoints().get(0).getTitle());
        assertEquals("章节C", result.getWeakPoints().get(1).getTitle());
        assertEquals("章节B", result.getWeakPoints().get(2).getTitle());
    }

    private StudyOverviewRawStats.ChapterAgg buildChapter(String code,
                                                          String title,
                                                          String subject,
                                                          int total,
                                                          int correct,
                                                          int wrong) {
        StudyOverviewRawStats.ChapterAgg agg = new StudyOverviewRawStats.ChapterAgg();
        agg.setChapterCode(code);
        agg.setChapterTitle(title);
        agg.setSubject(subject);
        agg.setTotalCount(total);
        agg.setCorrectCount(correct);
        agg.setWrongCount(wrong);
        return agg;
    }

    private String expectedWeekStart() {
        LocalDate today = LocalDate.now();
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).format(DATE_FORMAT);
    }

    private String expectedWeekEnd() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return weekStart.plusDays(6).format(DATE_FORMAT);
    }
}
