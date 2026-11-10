package com.sqx.app.base.user.badge.service.impl;

import com.sqx.app.base.user.badge.domain.vo.AppUserBadgeItemVO;
import com.sqx.app.base.user.badge.domain.vo.AppUserBadgeListVO;
import com.sqx.app.base.user.badge.service.UserBadgeService;
import com.sqx.app.base.user.stats.support.PracticeBadgeEvaluation;
import com.sqx.app.base.user.stats.support.PracticeBadgeRuleEvaluator;
import com.sqx.app.base.user.stats.support.UserStudyRawStats;
import com.sqx.app.base.user.stats.support.UserStudyStatsProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户端勋章：聚合练习记录后按里程碑规则判定解锁
 */
@Service
public class UserBadgeServiceImpl implements UserBadgeService {

    @Resource
    private UserStudyStatsProvider userStudyStatsProvider;

    @Resource
    private PracticeBadgeRuleEvaluator practiceBadgeRuleEvaluator;

    @Override
    public AppUserBadgeListVO listBadges(Long userId) {
        UserStudyRawStats raw = userStudyStatsProvider.aggregate(userId);
        List<PracticeBadgeEvaluation> evaluations = practiceBadgeRuleEvaluator.evaluateBadges(raw);

        List<AppUserBadgeItemVO> items = new ArrayList<>(evaluations.size());
        int unlockedCount = 0;
        for (PracticeBadgeEvaluation evaluation : evaluations) {
            AppUserBadgeItemVO item = new AppUserBadgeItemVO();
            item.setBadgeCode(evaluation.getBadgeCode());
            item.setName(evaluation.getName());
            item.setConditionText(evaluation.getConditionText());
            item.setUnlocked(evaluation.isUnlocked());
            item.setProgressValue(evaluation.getProgressValue());
            item.setTargetValue(evaluation.getTargetValue());
            items.add(item);
            if (evaluation.isUnlocked()) {
                unlockedCount++;
            }
        }

        AppUserBadgeListVO vo = new AppUserBadgeListVO();
        vo.setList(items);
        vo.setUnlockedCount(unlockedCount);
        return vo;
    }
}
