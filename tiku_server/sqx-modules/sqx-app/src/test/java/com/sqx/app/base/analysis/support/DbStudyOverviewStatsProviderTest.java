package com.sqx.app.base.analysis.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DbStudyOverviewStatsProvider 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DbStudyOverviewStatsProviderTest {

    @Mock
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Mock
    private StudyChapterMetadataSupport studyChapterMetadataSupport;

    @InjectMocks
    private DbStudyOverviewStatsProvider provider;

    @Test
    void aggregateOverview_whenNoRecords_shouldReturnZeroStats() {
        when(iUserPracticeRecordService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(studyChapterMetadataSupport.loadChapterMap(any())).thenReturn(Collections.emptyMap());

        StudyOverviewRawStats stats = provider.aggregateOverview(100L, LocalDate.of(2026, 8, 12));

        assertEquals(0, stats.getWeekQuestionCount());
        assertEquals(0, stats.getWeekCorrectCount());
        assertTrue(stats.getSubjectAggs().isEmpty());
        assertTrue(stats.getChapterAggs().isEmpty());
    }

    @Test
    void aggregateOverview_shouldAggregateWeekSubjectAndChapterStats() {
        UserPracticeRecord mathRecord = buildRecord(
                "chapter-rational-numbers", "有理数", "数学", 10, 8, 2,
                LocalDateTime.of(2026, 8, 11, 10, 0));
        UserPracticeRecord englishRecord = buildRecord(
                "chapter-english-grammar", "虚拟语气", "英语", 5, 2, 3,
                LocalDateTime.of(2026, 8, 10, 15, 0));

        when(iUserPracticeRecordService.list(any(Wrapper.class)))
                .thenReturn(Arrays.asList(mathRecord, englishRecord))
                .thenReturn(Collections.emptyList());
        when(studyChapterMetadataSupport.loadChapterMap(any())).thenReturn(Collections.emptyMap());
        when(studyChapterMetadataSupport.resolveSubject(any(), any())).thenAnswer(invocation -> {
            UserPracticeRecord record = invocation.getArgument(0);
            return record.getSubject();
        });
        when(studyChapterMetadataSupport.resolveChapterTitle(any(), any())).thenAnswer(invocation -> {
            UserPracticeRecord record = invocation.getArgument(0);
            return record.getChapterTitle();
        });
        when(studyChapterMetadataSupport.normalizeChapterCode(any())).thenAnswer(invocation -> {
            UserPracticeRecord record = invocation.getArgument(0);
            return record.getChapterCode();
        });

        StudyOverviewRawStats stats = provider.aggregateOverview(100L, LocalDate.of(2026, 8, 12));

        assertEquals(15, stats.getWeekQuestionCount());
        assertEquals(10, stats.getWeekCorrectCount());
        assertEquals(2, stats.getSubjectAggs().size());
        assertEquals(2, stats.getChapterAggs().size());
    }

    private UserPracticeRecord buildRecord(String chapterCode,
                                           String chapterTitle,
                                           String subject,
                                           int total,
                                           int correct,
                                           int wrong,
                                           LocalDateTime submitTime) {
        UserPracticeRecord record = new UserPracticeRecord();
        record.setChapterCode(chapterCode);
        record.setChapterTitle(chapterTitle);
        record.setSubject(subject);
        record.setTotalCount(total);
        record.setCorrectCount(correct);
        record.setWrongCount(wrong);
        record.setSubmitTime(submitTime);
        return record;
    }
}
