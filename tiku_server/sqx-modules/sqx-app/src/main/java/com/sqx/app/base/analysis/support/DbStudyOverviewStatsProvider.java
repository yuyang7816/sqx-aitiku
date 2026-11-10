package com.sqx.app.base.analysis.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 tb_user_practice_record 聚合本周题量、正确率、学科雷达与章薄弱点
 */
@Component
public class DbStudyOverviewStatsProvider implements StudyOverviewStatsProvider {

    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Resource
    private StudyChapterMetadataSupport studyChapterMetadataSupport;

    @Override
    public StudyOverviewRawStats aggregateOverview(Long userId, LocalDate refDate) {
        StudyOverviewRawStats stats = new StudyOverviewRawStats();
        if (userId == null || refDate == null) {
            return stats;
        }

        LocalDate weekStart = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);

        List<UserPracticeRecord> thisWeekRecords = listRecords(userId, weekStart, weekEnd);
        List<UserPracticeRecord> lastWeekRecords = listRecords(userId, lastWeekStart, lastWeekEnd);

        accumulateWeekTotals(stats, thisWeekRecords, true);
        accumulateWeekTotals(stats, lastWeekRecords, false);
        Map<String, QuestionChapter> chapterMap = studyChapterMetadataSupport.loadChapterMap(thisWeekRecords);
        stats.setSubjectAggs(aggregateBySubject(thisWeekRecords, chapterMap));
        stats.setChapterAggs(aggregateByChapter(thisWeekRecords, chapterMap));
        return stats;
    }

    private List<UserPracticeRecord> listRecords(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPracticeRecord::getUserId, userId)
                .ge(UserPracticeRecord::getSubmitTime, start)
                .le(UserPracticeRecord::getSubmitTime, end);
        List<UserPracticeRecord> records = iUserPracticeRecordService.list(wrapper);
        return records == null ? new ArrayList<>() : records;
    }

    private void accumulateWeekTotals(StudyOverviewRawStats stats,
                                        List<UserPracticeRecord> records,
                                        boolean thisWeek) {
        int totalCount = 0;
        int correctCount = 0;
        for (UserPracticeRecord record : records) {
            totalCount += safeInt(record.getTotalCount());
            correctCount += safeInt(record.getCorrectCount());
        }
        if (thisWeek) {
            stats.setWeekQuestionCount(totalCount);
            stats.setWeekCorrectCount(correctCount);
        } else {
            stats.setLastWeekQuestionCount(totalCount);
            stats.setLastWeekCorrectCount(correctCount);
        }
    }

    private List<StudyOverviewRawStats.SubjectAgg> aggregateBySubject(List<UserPracticeRecord> records,
                                                                    Map<String, QuestionChapter> chapterMap) {
        Map<String, StudyOverviewRawStats.SubjectAgg> map = new HashMap<>();
        for (UserPracticeRecord record : records) {
            String subject = studyChapterMetadataSupport.resolveSubject(record, chapterMap);
            StudyOverviewRawStats.SubjectAgg agg = map.computeIfAbsent(subject, key -> {
                StudyOverviewRawStats.SubjectAgg item = new StudyOverviewRawStats.SubjectAgg();
                item.setSubject(key);
                return item;
            });
            agg.setTotalCount(agg.getTotalCount() + safeInt(record.getTotalCount()));
            agg.setCorrectCount(agg.getCorrectCount() + safeInt(record.getCorrectCount()));
        }
        return new ArrayList<>(map.values());
    }

    private List<StudyOverviewRawStats.ChapterAgg> aggregateByChapter(List<UserPracticeRecord> records,
                                                                      Map<String, QuestionChapter> chapterMap) {
        Map<String, StudyOverviewRawStats.ChapterAgg> map = new HashMap<>();
        for (UserPracticeRecord record : records) {
            String chapterCode = studyChapterMetadataSupport.normalizeChapterCode(record);
            StudyOverviewRawStats.ChapterAgg agg = map.computeIfAbsent(chapterCode, key -> {
                StudyOverviewRawStats.ChapterAgg item = new StudyOverviewRawStats.ChapterAgg();
                item.setChapterCode(key);
                item.setChapterTitle(studyChapterMetadataSupport.resolveChapterTitle(record, chapterMap));
                item.setSubject(studyChapterMetadataSupport.resolveSubject(record, chapterMap));
                return item;
            });
            agg.setTotalCount(agg.getTotalCount() + safeInt(record.getTotalCount()));
            agg.setCorrectCount(agg.getCorrectCount() + safeInt(record.getCorrectCount()));
            agg.setWrongCount(agg.getWrongCount() + safeInt(record.getWrongCount()));
        }
        return new ArrayList<>(map.values());
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
