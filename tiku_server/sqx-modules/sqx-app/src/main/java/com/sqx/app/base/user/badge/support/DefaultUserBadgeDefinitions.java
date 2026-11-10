package com.sqx.app.base.user.badge.support;

import com.sqx.app.base.user.badge.constants.UserBadgeRuleType;
import com.sqx.app.base.user.badge.domain.entity.UserBadgeDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 内置 8 条里程碑勋章（DB 无数据或未建表时兜底）
 */
public final class DefaultUserBadgeDefinitions {

    private DefaultUserBadgeDefinitions() {
    }

    public static List<UserBadgeDefinition> list() {
        List<UserBadgeDefinition> list = new ArrayList<>(8);
        list.add(build("first_practice", "初出茅庐", "完成首次练习交卷", UserBadgeRuleType.SUBMIT_COUNT, 1, 1));
        list.add(build("study_3_days", "三日坚持", "累计学习满3天", UserBadgeRuleType.STUDY_DAYS, 3, 2));
        list.add(build("study_7_days", "一周达人", "累计学习满7天", UserBadgeRuleType.STUDY_DAYS, 7, 3));
        list.add(build("study_30_days", "月度学霸", "累计学习满30天", UserBadgeRuleType.STUDY_DAYS, 30, 4));
        list.add(build("question_50", "刷题新手", "累计做题满50道", UserBadgeRuleType.QUESTION_COUNT, 50, 5));
        list.add(build("question_100", "百题斩", "累计做题满100道", UserBadgeRuleType.QUESTION_COUNT, 100, 6));
        list.add(build("question_500", "题海王者", "累计做题满500道", UserBadgeRuleType.QUESTION_COUNT, 500, 7));
        list.add(build("perfect_score", "满分之星", "单次练习获得满分（100分）", UserBadgeRuleType.PERFECT_SCORE, 1, 8));
        return Collections.unmodifiableList(list);
    }

    private static UserBadgeDefinition build(String code, String name, String conditionText,
                                             String ruleType, int targetValue, int sortOrder) {
        UserBadgeDefinition def = new UserBadgeDefinition();
        def.setBadgeCode(code);
        def.setName(name);
        def.setConditionText(conditionText);
        def.setRuleType(ruleType);
        def.setTargetValue(targetValue);
        def.setSortOrder(sortOrder);
        def.setState(UserBadgeDefinition.STATE_ENABLED);
        return def;
    }
}
