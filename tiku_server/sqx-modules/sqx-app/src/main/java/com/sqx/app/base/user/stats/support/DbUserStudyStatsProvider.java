package com.sqx.app.base.user.stats.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 tb_user_practice_record 聚合累计学习天数、做题数与勋章判定所需指标
 */
@Component
public class DbUserStudyStatsProvider implements UserStudyStatsProvider {

    private static final int PERFECT_SCORE = 100;

    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Override
    public UserStudyRawStats aggregate(Long userId) {
        UserStudyRawStats stats = new UserStudyRawStats();
        if (userId == null) {
            return stats;
        }

        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPracticeRecord::getUserId, userId);
        List<UserPracticeRecord> records = iUserPracticeRecordService.list(wrapper);
        if (records == null || records.isEmpty()) {
            return stats;
        }

        Set<LocalDate> studyDates = new HashSet<>();
        int questionCount = 0;
        int submitCount = 0;
        boolean hasPerfectScore = false;

        for (UserPracticeRecord record : records) {
            submitCount++;
            questionCount += safeInt(record.getTotalCount());
            if (record.getSubmitTime() != null) {
                studyDates.add(record.getSubmitTime().toLocalDate());
            }
            if (!hasPerfectScore && safeInt(record.getScore()) >= PERFECT_SCORE) {
                hasPerfectScore = true;
            }
        }

        stats.setStudyDays(studyDates.size());
        stats.setQuestionCount(questionCount);
        stats.setSubmitCount(submitCount);
        stats.setHasPerfectScore(hasPerfectScore);
        return stats;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
