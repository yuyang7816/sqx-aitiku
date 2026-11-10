package com.sqx.app.base.user.stats.service.impl;

import com.sqx.app.base.user.stats.domain.vo.AppUserStatsVO;
import com.sqx.app.base.user.stats.support.PracticeBadgeRuleEvaluator;
import com.sqx.app.base.user.stats.support.UserStudyRawStats;
import com.sqx.app.base.user.stats.support.UserStudyStatsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/user/stats 学习统计单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserStatsServiceImplGetStatsTest {

    @Mock
    private UserStudyStatsProvider userStudyStatsProvider;

    @Mock
    private PracticeBadgeRuleEvaluator practiceBadgeRuleEvaluator;

    @InjectMocks
    private UserStatsServiceImpl userStatsService;

    @Test
    void getStats_whenNoPracticeData_shouldReturnZeros() {
        when(userStudyStatsProvider.aggregate(100L)).thenReturn(new UserStudyRawStats());
        when(practiceBadgeRuleEvaluator.countUnlockedBadges(org.mockito.ArgumentMatchers.any())).thenReturn(0);

        AppUserStatsVO result = userStatsService.getStats(100L);

        assertNotNull(result);
        assertEquals(0, result.getStudyDays());
        assertEquals(0, result.getQuestionCount());
        assertEquals(0, result.getBadgeCount());
        verify(userStudyStatsProvider).aggregate(eq(100L));
    }

    @Test
    void getStats_shouldMapAggregatedStatsToVo() {
        UserStudyRawStats raw = new UserStudyRawStats();
        raw.setStudyDays(12);
        raw.setQuestionCount(1256);
        raw.setSubmitCount(48);
        raw.setHasPerfectScore(true);

        when(userStudyStatsProvider.aggregate(200L)).thenReturn(raw);
        when(practiceBadgeRuleEvaluator.countUnlockedBadges(raw)).thenReturn(8);

        AppUserStatsVO result = userStatsService.getStats(200L);

        assertEquals(12, result.getStudyDays());
        assertEquals(1256, result.getQuestionCount());
        assertEquals(8, result.getBadgeCount());
        verify(practiceBadgeRuleEvaluator).countUnlockedBadges(raw);
    }
}
