package com.sqx.app.base.user.badge.constants;

/**
 * 勋章解锁规则类型（与 user_badge_definition.rule_type 一致）
 */
public final class UserBadgeRuleType {

    private UserBadgeRuleType() {
    }

    public static final String SUBMIT_COUNT = "submit_count";
    public static final String STUDY_DAYS = "study_days";
    public static final String QUESTION_COUNT = "question_count";
    public static final String PERFECT_SCORE = "perfect_score";
}
