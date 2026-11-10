package com.sqx.app.base.analysis.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisTodayVO;
import com.sqx.app.base.analysis.support.StudyTodayRawStats;
import com.sqx.app.base.analysis.support.StudyTodayStatsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/analysis/today 今日学情统计单元测试
 */
@ExtendWith(MockitoExtension.class)
class AnalysisTodayServiceImplGetTodayTest {

    @Mock
    private StudyTodayStatsProvider studyTodayStatsProvider;

    @InjectMocks
    private AnalysisTodayServiceImpl analysisTodayService;

    @Test
    void getTodayStats_whenNoPracticeData_shouldReturnZeroDurationAndQuestionCount() {
        StudyTodayRawStats raw = new StudyTodayRawStats();
        raw.setDurationMinutes(0);
        raw.setQuestionCount(0);
        when(studyTodayStatsProvider.aggregateToday(eq(100L), any(LocalDate.class))).thenReturn(raw);

        AppAnalysisTodayVO result = analysisTodayService.getTodayStats(100L);

        assertNotNull(result);
        assertEquals(LocalDate.now().toString(), result.getStatDate());
        assertEquals(0, result.getDurationMinutes());
        assertEquals(0, result.getQuestionCount());
        verify(studyTodayStatsProvider).aggregateToday(eq(100L), any(LocalDate.class));
    }

    @Test
    void getTodayStats_whenProviderReturnsAggregatedValues_shouldMapToVo() {
        StudyTodayRawStats raw = new StudyTodayRawStats();
        raw.setDurationMinutes(35);
        raw.setQuestionCount(12);
        when(studyTodayStatsProvider.aggregateToday(eq(200L), any(LocalDate.class))).thenReturn(raw);

        AppAnalysisTodayVO result = analysisTodayService.getTodayStats(200L);

        assertEquals(35, result.getDurationMinutes());
        assertEquals(12, result.getQuestionCount());
        assertEquals(LocalDate.now().toString(), result.getStatDate());
    }

    @Test
    void getTodayStats_whenProviderReturnsNegativeValues_shouldClampToZero() {
        StudyTodayRawStats raw = new StudyTodayRawStats();
        raw.setDurationMinutes(-5);
        raw.setQuestionCount(-1);
        when(studyTodayStatsProvider.aggregateToday(eq(300L), any(LocalDate.class))).thenReturn(raw);

        AppAnalysisTodayVO result = analysisTodayService.getTodayStats(300L);

        assertEquals(0, result.getDurationMinutes());
        assertEquals(0, result.getQuestionCount());
    }
}
