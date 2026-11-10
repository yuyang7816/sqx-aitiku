package com.sqx.app.base.analysis.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 基于 tb_user_practice_record 聚合今日练习时长与题量
 */
@Component
public class DbStudyTodayStatsProvider implements StudyTodayStatsProvider {

    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Override
    public StudyTodayRawStats aggregateToday(Long userId, LocalDate statDate) {
        StudyTodayRawStats stats = new StudyTodayRawStats();
        if (userId == null || statDate == null) {
            return stats;
        }

        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPracticeRecord::getUserId, userId)
                .ge(UserPracticeRecord::getSubmitTime, start)
                .le(UserPracticeRecord::getSubmitTime, end);
        List<UserPracticeRecord> records = iUserPracticeRecordService.list(wrapper);

        int durationSeconds = 0;
        int questionCount = 0;
        for (UserPracticeRecord record : records) {
            durationSeconds += Math.max(0, record.getDurationSeconds() == null ? 0 : record.getDurationSeconds());
            questionCount += Math.max(0, record.getTotalCount() == null ? 0 : record.getTotalCount());
        }

        stats.setDurationMinutes(durationSeconds / 60);
        stats.setQuestionCount(questionCount);
        return stats;
    }
}
