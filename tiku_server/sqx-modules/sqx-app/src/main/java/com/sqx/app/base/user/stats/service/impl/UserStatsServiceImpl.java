package com.sqx.app.base.user.stats.service.impl;

import com.sqx.app.base.user.stats.domain.vo.AppUserStatsVO;
import com.sqx.app.base.user.stats.service.UserStatsService;
import com.sqx.app.base.user.stats.support.PracticeBadgeRuleEvaluator;
import com.sqx.app.base.user.stats.support.UserStudyRawStats;
import com.sqx.app.base.user.stats.support.UserStudyStatsProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户端学习统计：经 {@link UserStudyStatsProvider} 聚合练习记录
 */
@Service
public class UserStatsServiceImpl implements UserStatsService {

    @Resource
    private UserStudyStatsProvider userStudyStatsProvider;

    @Resource
    private PracticeBadgeRuleEvaluator practiceBadgeRuleEvaluator;

    @Override
    public AppUserStatsVO getStats(Long userId) {
        UserStudyRawStats raw = userStudyStatsProvider.aggregate(userId);

        AppUserStatsVO vo = new AppUserStatsVO();
        vo.setStudyDays(Math.max(0, raw.getStudyDays()));
        vo.setQuestionCount(Math.max(0, raw.getQuestionCount()));
        vo.setBadgeCount(Math.max(0, practiceBadgeRuleEvaluator.countUnlockedBadges(raw)));
        return vo;
    }
}
