package com.sqx.app.base.user.stats.support;

import com.sqx.app.base.user.badge.constants.UserBadgeRuleType;
import com.sqx.app.base.user.badge.domain.entity.UserBadgeDefinition;
import com.sqx.app.base.user.badge.support.DefaultUserBadgeDefinitions;
import com.sqx.app.base.user.badge.support.UserBadgeDefinitionProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于练习记录里程碑计算勋章解锁（与 GET /app/user/badges、GET /app/user/stats 规则一致）
 */
@Component
public class PracticeBadgeRuleEvaluator {

    @Resource
    private UserBadgeDefinitionProvider userBadgeDefinitionProvider;

    /**
     * 按练习记录聚合结果统计已解锁勋章数
     */
    public int countUnlockedBadges(UserStudyRawStats stats) {
        if (stats == null) {
            return 0;
        }
        int count = 0;
        for (PracticeBadgeEvaluation evaluation : evaluateBadges(stats)) {
            if (evaluation.isUnlocked()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 返回全部勋章及当前用户解锁状态
     */
    public List<PracticeBadgeEvaluation> evaluateBadges(UserStudyRawStats stats) {
        UserStudyRawStats safeStats = stats == null ? new UserStudyRawStats() : stats;
        int submitCount = Math.max(0, safeStats.getSubmitCount());
        int studyDays = Math.max(0, safeStats.getStudyDays());
        int questionCount = Math.max(0, safeStats.getQuestionCount());
        boolean hasPerfectScore = safeStats.isHasPerfectScore();

        List<UserBadgeDefinition> definitions = resolveDefinitions();
        List<PracticeBadgeEvaluation> list = new ArrayList<>(definitions.size());
        for (UserBadgeDefinition definition : definitions) {
            list.add(evaluateDefinition(definition, submitCount, studyDays, questionCount, hasPerfectScore));
        }
        return Collections.unmodifiableList(list);
    }

    List<UserBadgeDefinition> resolveDefinitions() {
        if (userBadgeDefinitionProvider != null) {
            return userBadgeDefinitionProvider.listActiveDefinitions();
        }
        return DefaultUserBadgeDefinitions.list();
    }

    private PracticeBadgeEvaluation evaluateDefinition(UserBadgeDefinition definition, int submitCount,
                                                       int studyDays, int questionCount, boolean hasPerfectScore) {
        String ruleType = definition.getRuleType();
        int targetValue = Math.max(1, definition.getTargetValue() == null ? 1 : definition.getTargetValue());
        int progressValue;
        boolean unlocked;
        if (UserBadgeRuleType.PERFECT_SCORE.equals(ruleType)) {
            progressValue = hasPerfectScore ? 1 : 0;
            unlocked = hasPerfectScore;
        } else if (UserBadgeRuleType.STUDY_DAYS.equals(ruleType)) {
            progressValue = studyDays;
            unlocked = studyDays >= targetValue;
        } else if (UserBadgeRuleType.QUESTION_COUNT.equals(ruleType)) {
            progressValue = questionCount;
            unlocked = questionCount >= targetValue;
        } else {
            progressValue = submitCount;
            unlocked = submitCount >= targetValue;
        }

        PracticeBadgeEvaluation evaluation = new PracticeBadgeEvaluation();
        evaluation.setBadgeCode(definition.getBadgeCode());
        evaluation.setName(definition.getName());
        evaluation.setConditionText(definition.getConditionText());
        evaluation.setProgressValue(progressValue);
        evaluation.setTargetValue(targetValue);
        evaluation.setUnlocked(unlocked);
        return evaluation;
    }
}
