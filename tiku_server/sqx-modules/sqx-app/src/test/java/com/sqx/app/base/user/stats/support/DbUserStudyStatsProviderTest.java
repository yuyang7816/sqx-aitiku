package com.sqx.app.base.user.stats.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DbUserStudyStatsProvider 练习记录聚合单元测试
 */
@ExtendWith(MockitoExtension.class)
class DbUserStudyStatsProviderTest {

    @Mock
    private IUserPracticeRecordService iUserPracticeRecordService;

    @InjectMocks
    private DbUserStudyStatsProvider dbUserStudyStatsProvider;

    @Test
    void aggregate_whenNoRecords_shouldReturnZeros() {
        when(iUserPracticeRecordService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        UserStudyRawStats stats = dbUserStudyStatsProvider.aggregate(100L);

        assertEquals(0, stats.getStudyDays());
        assertEquals(0, stats.getQuestionCount());
        assertEquals(0, stats.getSubmitCount());
        assertFalse(stats.isHasPerfectScore());
    }

    @Test
    void aggregate_shouldCountDistinctStudyDaysAndQuestionTotal() {
        UserPracticeRecord day1 = buildRecord(LocalDateTime.of(2026, 8, 10, 10, 0), 20, 80);
        UserPracticeRecord day1Again = buildRecord(LocalDateTime.of(2026, 8, 10, 18, 0), 15, 100);
        UserPracticeRecord day2 = buildRecord(LocalDateTime.of(2026, 8, 11, 9, 0), 10, 90);

        when(iUserPracticeRecordService.list(any(Wrapper.class)))
                .thenReturn(Arrays.asList(day1, day1Again, day2));

        UserStudyRawStats stats = dbUserStudyStatsProvider.aggregate(200L);

        assertEquals(2, stats.getStudyDays());
        assertEquals(45, stats.getQuestionCount());
        assertEquals(3, stats.getSubmitCount());
        assertTrue(stats.isHasPerfectScore());
    }

    private UserPracticeRecord buildRecord(LocalDateTime submitTime, int totalCount, int score) {
        UserPracticeRecord record = new UserPracticeRecord();
        record.setUserId(200L);
        record.setSubmitTime(submitTime);
        record.setTotalCount(totalCount);
        record.setScore(score);
        return record;
    }
}
