package com.sqx.app.base.user.stats.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 练习记录勋章里程碑规则单元测试
 */
class PracticeBadgeRuleEvaluatorTest {

    private final PracticeBadgeRuleEvaluator evaluator = new PracticeBadgeRuleEvaluator();

    @Test
    void countUnlockedBadges_whenEmpty_shouldReturnZero() {
        assertEquals(0, evaluator.countUnlockedBadges(new UserStudyRawStats()));
        assertEquals(0, evaluator.countUnlockedBadges(null));
    }

    @Test
    void countUnlockedBadges_firstPracticeOnly_shouldReturnOne() {
        UserStudyRawStats stats = new UserStudyRawStats();
        stats.setSubmitCount(1);
        stats.setStudyDays(1);
        stats.setQuestionCount(5);

        assertEquals(1, evaluator.countUnlockedBadges(stats));
    }

    @Test
    void countUnlockedBadges_allMilestonesMet_shouldReturnEight() {
        UserStudyRawStats stats = new UserStudyRawStats();
        stats.setSubmitCount(100);
        stats.setStudyDays(30);
        stats.setQuestionCount(500);
        stats.setHasPerfectScore(true);

        assertEquals(8, evaluator.countUnlockedBadges(stats));
    }

    @Test
    void countUnlockedBadges_partialMilestones_shouldCountMatchedOnly() {
        UserStudyRawStats stats = new UserStudyRawStats();
        stats.setSubmitCount(3);
        stats.setStudyDays(7);
        stats.setQuestionCount(120);

        assertEquals(5, evaluator.countUnlockedBadges(stats));
    }

    @Test
    void evaluateBadges_whenEmpty_shouldReturnEightLockedBadges() {
        List<PracticeBadgeEvaluation> badges = evaluator.evaluateBadges(new UserStudyRawStats());

        assertEquals(8, badges.size());
        assertTrue(badges.stream().noneMatch(PracticeBadgeEvaluation::isUnlocked));
        assertEquals("first_practice", badges.get(0).getBadgeCode());
        assertEquals("完成首次练习交卷", badges.get(0).getConditionText());
    }

    @Test
    void evaluateBadges_firstPracticeOnly_shouldUnlockFirstBadge() {
        UserStudyRawStats stats = new UserStudyRawStats();
        stats.setSubmitCount(1);
        stats.setStudyDays(1);
        stats.setQuestionCount(5);

        List<PracticeBadgeEvaluation> badges = evaluator.evaluateBadges(stats);

        assertTrue(badges.get(0).isUnlocked());
        assertEquals(1, badges.get(0).getProgressValue());
        assertFalse(badges.get(1).isUnlocked());
        assertEquals(1, badges.get(1).getProgressValue());
    }

    @Test
    void evaluateBadges_allMilestonesMet_shouldUnlockAll() {
        UserStudyRawStats stats = new UserStudyRawStats();
        stats.setSubmitCount(100);
        stats.setStudyDays(30);
        stats.setQuestionCount(500);
        stats.setHasPerfectScore(true);

        List<PracticeBadgeEvaluation> badges = evaluator.evaluateBadges(stats);

        assertEquals(8, badges.stream().filter(PracticeBadgeEvaluation::isUnlocked).count());
        assertTrue(badges.get(7).isUnlocked());
        assertEquals("perfect_score", badges.get(7).getBadgeCode());
    }
}
