package com.sqx.app.base.analysis.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisPeerComparisonVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportAdviceVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportSubjectBarVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.support.StudyReportRawStats;
import com.sqx.app.base.analysis.support.StudyReportStatsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/analysis/report 月度报告单元测试
 */
@ExtendWith(MockitoExtension.class)
class AnalysisReportServiceImplGetReportTest {

    @Mock
    private StudyReportStatsProvider studyReportStatsProvider;

    @InjectMocks
    private AnalysisReportServiceImpl analysisReportService;

    @Test
    void getReport_whenNoPracticeData_shouldReturnEmptyBarsAndDefaultPeerRank() {
        when(studyReportStatsProvider.aggregateReport(eq(100L), eq(YearMonth.of(2026, 6))))
                .thenReturn(new StudyReportRawStats());

        AppAnalysisReportVO result = analysisReportService.getReport(100L, YearMonth.of(2026, 6));

        assertNotNull(result);
        assertEquals("2026-06", result.getMonth());
        assertEquals("2026年6月 · 学习报告", result.getMonthTitle());
        assertEquals("2026-06-01", result.getMonthStartDate());
        assertEquals("2026-06-30", result.getMonthEndDate());
        assertEquals(0, result.getMonthQuestionCount());
        assertEquals(0, result.getCorrectRate());
        assertTrue(result.getSubjectBars().isEmpty());
        assertEquals(3, result.getAiAdvices().size());
        assertNotNull(result.getPeerComparison());
        assertEquals(50, result.getRankPercent());
        assertEquals("超越了 50% 的同龄人", result.getRankTitle());
        verify(studyReportStatsProvider).aggregateReport(eq(100L), eq(YearMonth.of(2026, 6)));
    }

    @Test
    void getReport_shouldMapSubjectBarsPeerComparisonAndAdvices() {
        StudyReportRawStats raw = buildSampleRawStats();
        when(studyReportStatsProvider.aggregateReport(eq(200L), eq(YearMonth.of(2026, 8))))
                .thenReturn(raw);

        AppAnalysisReportVO result = analysisReportService.getReport(200L, YearMonth.of(2026, 8));

        assertEquals(25, result.getMonthQuestionCount());
        assertEquals(68, result.getCorrectRate());
        assertEquals(2, result.getSubjectBars().size());

        AppAnalysisReportSubjectBarVO mathBar = result.getSubjectBars().get(0);
        assertEquals("数学", mathBar.getLabel());
        assertEquals(83, mathBar.getCorrectRate());
        assertEquals("83%", mathBar.getPercent());

        AppAnalysisPeerComparisonVO peer = result.getPeerComparison();
        assertNotNull(peer);
        assertEquals("初三", peer.getGradeLabel());
        assertEquals(68, peer.getUserCorrectRate());
        assertEquals(50, peer.getBeatPercent());
        assertEquals(3, peer.getPeerSampleCount());

        assertEquals(3, result.getAiAdvices().size());
        AppAnalysisReportAdviceVO firstAdvice = result.getAiAdvices().get(0);
        assertTrue(firstAdvice.getContent().contains("虚拟语气"));
        assertEquals("wrong-practice", firstAdvice.getAction());
    }

    @Test
    void getReport_shouldIncludeStaleWrongAdvice() {
        StudyReportRawStats raw = new StudyReportRawStats();
        raw.setMonthQuestionCount(10);
        raw.setMonthCorrectCount(8);
        raw.setStaleWrongCount(5);
        when(studyReportStatsProvider.aggregateReport(eq(300L), eq(YearMonth.of(2026, 7))))
                .thenReturn(raw);

        AppAnalysisReportVO result = analysisReportService.getReport(300L, YearMonth.of(2026, 7));

        assertTrue(result.getAiAdvices().stream()
                .anyMatch(item -> item.getContent().contains("错题本中有 5 道题")));
    }

    private StudyReportRawStats buildSampleRawStats() {
        StudyReportRawStats raw = new StudyReportRawStats();
        raw.setMonthQuestionCount(25);
        raw.setMonthCorrectCount(17);
        raw.setLastMonthQuestionCount(10);
        raw.setLastMonthCorrectCount(8);
        raw.setGradeShort("初三");
        raw.setStaleWrongCount(2);

        StudyReportRawStats.SubjectAgg math = new StudyReportRawStats.SubjectAgg();
        math.setSubject("数学");
        math.setTotalCount(12);
        math.setCorrectCount(10);

        StudyReportRawStats.SubjectAgg english = new StudyReportRawStats.SubjectAgg();
        english.setSubject("英语");
        english.setTotalCount(13);
        english.setCorrectCount(7);
        raw.setSubjectAggs(Arrays.asList(math, english));

        StudyReportRawStats.ChapterAgg weakChapter = new StudyReportRawStats.ChapterAgg();
        weakChapter.setChapterCode("chapter-english-grammar");
        weakChapter.setChapterTitle("虚拟语气");
        weakChapter.setSubject("英语");
        weakChapter.setTotalCount(8);
        weakChapter.setCorrectCount(2);
        weakChapter.setWrongCount(6);
        raw.setChapterAggs(Collections.singletonList(weakChapter));

        StudyReportRawStats.PeerUserScore self = new StudyReportRawStats.PeerUserScore();
        self.setUserId(200L);
        self.setQuestionCount(25);
        self.setCorrectCount(17);

        StudyReportRawStats.PeerUserScore peerA = new StudyReportRawStats.PeerUserScore();
        peerA.setUserId(201L);
        peerA.setQuestionCount(30);
        peerA.setCorrectCount(27);

        StudyReportRawStats.PeerUserScore peerB = new StudyReportRawStats.PeerUserScore();
        peerB.setUserId(202L);
        peerB.setQuestionCount(20);
        peerB.setCorrectCount(10);

        raw.setPeerScores(Arrays.asList(self, peerA, peerB));
        return raw;
    }
}
