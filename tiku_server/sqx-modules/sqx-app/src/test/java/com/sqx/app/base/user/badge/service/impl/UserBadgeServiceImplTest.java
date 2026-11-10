package com.sqx.app.base.user.badge.service.impl;

import com.sqx.app.base.user.badge.domain.vo.AppUserBadgeListVO;
import com.sqx.app.base.user.stats.support.PracticeBadgeEvaluation;
import com.sqx.app.base.user.stats.support.PracticeBadgeRuleEvaluator;
import com.sqx.app.base.user.stats.support.UserStudyRawStats;
import com.sqx.app.base.user.stats.support.UserStudyStatsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/user/badges 勋章列表单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserBadgeServiceImplTest {

    @Mock
    private UserStudyStatsProvider userStudyStatsProvider;

    @Mock
    private PracticeBadgeRuleEvaluator practiceBadgeRuleEvaluator;

    @InjectMocks
    private UserBadgeServiceImpl userBadgeService;

    @Test
    void listBadges_whenNoPracticeData_shouldReturnLockedBadges() {
        when(userStudyStatsProvider.aggregate(100L)).thenReturn(new UserStudyRawStats());

        PracticeBadgeEvaluation locked = new PracticeBadgeEvaluation();
        locked.setBadgeCode("first_practice");
        locked.setName("初出茅庐");
        locked.setConditionText("完成首次练习交卷");
        locked.setProgressValue(0);
        locked.setTargetValue(1);
        locked.setUnlocked(false);
        when(practiceBadgeRuleEvaluator.evaluateBadges(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(locked));

        AppUserBadgeListVO result = userBadgeService.listBadges(100L);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertEquals(0, result.getUnlockedCount());
        assertEquals("first_practice", result.getList().get(0).getBadgeCode());
        assertFalse(result.getList().get(0).getUnlocked());
        verify(userStudyStatsProvider).aggregate(eq(100L));
    }

    @Test
    void listBadges_shouldMapEvaluationsToVo() {
        UserStudyRawStats raw = new UserStudyRawStats();
        raw.setSubmitCount(1);

        PracticeBadgeEvaluation unlocked = new PracticeBadgeEvaluation();
        unlocked.setBadgeCode("first_practice");
        unlocked.setName("初出茅庐");
        unlocked.setConditionText("完成首次练习交卷");
        unlocked.setProgressValue(1);
        unlocked.setTargetValue(1);
        unlocked.setUnlocked(true);

        PracticeBadgeEvaluation locked = new PracticeBadgeEvaluation();
        locked.setBadgeCode("study_3_days");
        locked.setName("三日坚持");
        locked.setConditionText("累计学习满3天");
        locked.setProgressValue(1);
        locked.setTargetValue(3);
        locked.setUnlocked(false);

        when(userStudyStatsProvider.aggregate(200L)).thenReturn(raw);
        when(practiceBadgeRuleEvaluator.evaluateBadges(raw))
                .thenReturn(java.util.Arrays.asList(unlocked, locked));

        AppUserBadgeListVO result = userBadgeService.listBadges(200L);

        assertEquals(2, result.getList().size());
        assertEquals(1, result.getUnlockedCount());
        assertTrue(result.getList().get(0).getUnlocked());
        assertFalse(result.getList().get(1).getUnlocked());
        assertEquals(1, result.getList().get(1).getProgressValue());
        verify(practiceBadgeRuleEvaluator).evaluateBadges(raw);
    }
}
