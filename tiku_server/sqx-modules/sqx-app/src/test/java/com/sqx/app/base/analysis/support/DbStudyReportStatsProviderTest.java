package com.sqx.app.base.analysis.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IUserPracticeRecordService;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DbStudyReportStatsProvider 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DbStudyReportStatsProviderTest {

    @Mock
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Mock
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Mock
    private IUserService iUserService;

    @Mock
    private StudyChapterMetadataSupport studyChapterMetadataSupport;

    @InjectMocks
    private DbStudyReportStatsProvider provider;

    @Test
    void aggregateReport_whenNoRecords_shouldReturnZeroStats() {
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setGradeShort("初三");

        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(user));
        when(iUserPracticeRecordService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());
        when(iUserQuestionWrongService.count(any(Wrapper.class))).thenReturn(0L);
        when(studyChapterMetadataSupport.loadChapterMap(any())).thenReturn(Collections.emptyMap());

        StudyReportRawStats stats = provider.aggregateReport(100L, YearMonth.of(2026, 6));

        assertEquals(0, stats.getMonthQuestionCount());
        assertEquals(0, stats.getMonthCorrectCount());
        assertTrue(stats.getSubjectAggs().isEmpty());
        assertEquals("初三", stats.getGradeShort());
        assertEquals(0, stats.getStaleWrongCount());
    }

    @Test
    void aggregateReport_shouldAggregateMonthSubjectChapterAndPeerScores() {
        UserEntity user = new UserEntity();
        user.setUserId(100L);
        user.setGradeShort("初三");

        UserEntity peer = new UserEntity();
        peer.setUserId(101L);
        peer.setGradeShort("初三");

        UserPracticeRecord mathRecord = buildRecord(
                100L, "chapter-rational-numbers", "有理数", "数学", 10, 8, 2,
                LocalDateTime.of(2026, 6, 11, 10, 0));
        UserPracticeRecord englishRecord = buildRecord(
                100L, "chapter-english-grammar", "虚拟语气", "英语", 5, 2, 3,
                LocalDateTime.of(2026, 6, 10, 15, 0));
        UserPracticeRecord peerRecord = buildRecord(
                101L, "chapter-physics", "力学", "物理", 8, 6, 2,
                LocalDateTime.of(2026, 6, 9, 12, 0));

        when(iUserService.getById(100L)).thenReturn(user);
        when(iUserService.list(any(Wrapper.class))).thenReturn(Arrays.asList(user, peer));
        when(iUserPracticeRecordService.list(any(Wrapper.class)))
                .thenReturn(Arrays.asList(mathRecord, englishRecord))
                .thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList(mathRecord, englishRecord, peerRecord));
        when(iUserQuestionWrongService.count(any(Wrapper.class))).thenReturn(3L);
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

        StudyReportRawStats stats = provider.aggregateReport(100L, YearMonth.of(2026, 6));

        assertEquals(15, stats.getMonthQuestionCount());
        assertEquals(10, stats.getMonthCorrectCount());
        assertEquals(2, stats.getSubjectAggs().size());
        assertEquals(2, stats.getChapterAggs().size());
        assertEquals(3, stats.getStaleWrongCount());
        assertEquals(2, stats.getPeerScores().size());
    }

    private UserPracticeRecord buildRecord(Long userId,
                                           String chapterCode,
                                           String chapterTitle,
                                           String subject,
                                           int total,
                                           int correct,
                                           int wrong,
                                           LocalDateTime submitTime) {
        UserPracticeRecord record = new UserPracticeRecord();
        record.setUserId(userId);
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
