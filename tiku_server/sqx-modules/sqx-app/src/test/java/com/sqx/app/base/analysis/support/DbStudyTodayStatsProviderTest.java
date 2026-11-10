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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DbStudyTodayStatsProvider 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DbStudyTodayStatsProviderTest {

    @Mock
    private IUserPracticeRecordService iUserPracticeRecordService;

    @InjectMocks
    private DbStudyTodayStatsProvider provider;

    @Test
    void aggregateToday_whenNoRecords_shouldReturnZero() {
        when(iUserPracticeRecordService.list(any(Wrapper.class))).thenReturn(Arrays.asList());

        StudyTodayRawStats stats = provider.aggregateToday(100L, LocalDate.of(2026, 8, 5));

        assertEquals(0, stats.getDurationMinutes());
        assertEquals(0, stats.getQuestionCount());
    }

    @Test
    void aggregateToday_shouldSumDurationAndQuestionCount() {
        UserPracticeRecord r1 = new UserPracticeRecord();
        r1.setDurationSeconds(900);
        r1.setTotalCount(3);
        r1.setSubmitTime(LocalDateTime.of(2026, 8, 5, 10, 0));

        UserPracticeRecord r2 = new UserPracticeRecord();
        r2.setDurationSeconds(300);
        r2.setTotalCount(2);
        r2.setSubmitTime(LocalDateTime.of(2026, 8, 5, 15, 0));

        when(iUserPracticeRecordService.list(any(Wrapper.class))).thenReturn(Arrays.asList(r1, r2));

        StudyTodayRawStats stats = provider.aggregateToday(100L, LocalDate.of(2026, 8, 5));

        assertEquals(20, stats.getDurationMinutes());
        assertEquals(5, stats.getQuestionCount());
    }
}
