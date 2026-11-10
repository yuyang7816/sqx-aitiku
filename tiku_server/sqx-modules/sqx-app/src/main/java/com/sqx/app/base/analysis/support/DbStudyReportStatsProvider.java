package com.sqx.app.base.analysis.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IUserPracticeRecordService;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 tb_user_practice_record、tb_user_question_wrong、tb_user 聚合月度报告数据
 */
@Component
public class DbStudyReportStatsProvider implements StudyReportStatsProvider {

    private static final int STALE_WRONG_DAYS = 3;

    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Resource
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Resource
    private IUserService iUserService;

    @Resource
    private StudyChapterMetadataSupport studyChapterMetadataSupport;

    @Override
    public StudyReportRawStats aggregateReport(Long userId, YearMonth month) {
        StudyReportRawStats stats = new StudyReportRawStats();
        if (userId == null || month == null) {
            return stats;
        }

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        YearMonth lastMonth = month.minusMonths(1);
        LocalDate lastMonthStart = lastMonth.atDay(1);
        LocalDate lastMonthEnd = lastMonth.atEndOfMonth();

        List<UserPracticeRecord> monthRecords = listRecords(userId, monthStart, monthEnd);
        List<UserPracticeRecord> lastMonthRecords = listRecords(userId, lastMonthStart, lastMonthEnd);

        accumulateMonthTotals(stats, monthRecords, true);
        accumulateMonthTotals(stats, lastMonthRecords, false);
        Map<String, QuestionChapter> chapterMap = studyChapterMetadataSupport.loadChapterMap(monthRecords);
        stats.setSubjectAggs(aggregateBySubject(monthRecords, chapterMap));
        stats.setChapterAggs(aggregateByChapter(monthRecords, chapterMap));
        stats.setStaleWrongCount(countStaleWrongQuestions(userId));

        UserEntity user = iUserService.getById(userId);
        if (user != null) {
            stats.setGrade(StringUtils.trimToEmpty(user.getGrade()));
            stats.setGradeShort(StringUtils.trimToEmpty(user.getGradeShort()));
        }
        stats.setPeerScores(aggregatePeerScores(userId, month, user));
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

    private void accumulateMonthTotals(StudyReportRawStats stats,
                                       List<UserPracticeRecord> records,
                                       boolean thisMonth) {
        int totalCount = 0;
        int correctCount = 0;
        for (UserPracticeRecord record : records) {
            totalCount += safeInt(record.getTotalCount());
            correctCount += safeInt(record.getCorrectCount());
        }
        if (thisMonth) {
            stats.setMonthQuestionCount(totalCount);
            stats.setMonthCorrectCount(correctCount);
        } else {
            stats.setLastMonthQuestionCount(totalCount);
            stats.setLastMonthCorrectCount(correctCount);
        }
    }

    private List<StudyReportRawStats.SubjectAgg> aggregateBySubject(List<UserPracticeRecord> records,
                                                                  Map<String, QuestionChapter> chapterMap) {
        Map<String, StudyReportRawStats.SubjectAgg> map = new HashMap<>();
        for (UserPracticeRecord record : records) {
            String subject = studyChapterMetadataSupport.resolveSubject(record, chapterMap);
            StudyReportRawStats.SubjectAgg agg = map.computeIfAbsent(subject, key -> {
                StudyReportRawStats.SubjectAgg item = new StudyReportRawStats.SubjectAgg();
                item.setSubject(key);
                return item;
            });
            agg.setTotalCount(agg.getTotalCount() + safeInt(record.getTotalCount()));
            agg.setCorrectCount(agg.getCorrectCount() + safeInt(record.getCorrectCount()));
        }
        return new ArrayList<>(map.values());
    }

    private List<StudyReportRawStats.ChapterAgg> aggregateByChapter(List<UserPracticeRecord> records,
                                                                  Map<String, QuestionChapter> chapterMap) {
        Map<String, StudyReportRawStats.ChapterAgg> map = new HashMap<>();
        for (UserPracticeRecord record : records) {
            String chapterCode = studyChapterMetadataSupport.normalizeChapterCode(record);
            StudyReportRawStats.ChapterAgg agg = map.computeIfAbsent(chapterCode, key -> {
                StudyReportRawStats.ChapterAgg item = new StudyReportRawStats.ChapterAgg();
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

    private int countStaleWrongQuestions(Long userId) {
        LocalDateTime staleBefore = LocalDateTime.now().minusDays(STALE_WRONG_DAYS);
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId)
                .eq(UserQuestionWrong::getMastered, UserQuestionWrong.MASTERED_NO)
                .and(w -> w.isNull(UserQuestionWrong::getLastPracticeAt)
                        .or()
                        .lt(UserQuestionWrong::getLastPracticeAt, staleBefore));
        Long count = iUserQuestionWrongService.count(wrapper);
        return count == null ? 0 : Math.max(0, count.intValue());
    }

    private List<StudyReportRawStats.PeerUserScore> aggregatePeerScores(Long userId,
                                                                        YearMonth month,
                                                                        UserEntity user) {
        Set<Long> peerUserIds = resolvePeerUserIds(userId, user);
        if (peerUserIds.isEmpty()) {
            peerUserIds.add(userId);
        }

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        LocalDateTime start = monthStart.atStartOfDay();
        LocalDateTime end = monthEnd.atTime(LocalTime.MAX);

        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserPracticeRecord::getUserId, peerUserIds)
                .ge(UserPracticeRecord::getSubmitTime, start)
                .le(UserPracticeRecord::getSubmitTime, end);
        List<UserPracticeRecord> records = iUserPracticeRecordService.list(wrapper);
        if (records == null || records.isEmpty()) {
            StudyReportRawStats.PeerUserScore self = new StudyReportRawStats.PeerUserScore();
            self.setUserId(userId);
            return java.util.Collections.singletonList(self);
        }

        Map<Long, StudyReportRawStats.PeerUserScore> scoreMap = new HashMap<>();
        for (UserPracticeRecord record : records) {
            Long recordUserId = record.getUserId();
            if (recordUserId == null) {
                continue;
            }
            StudyReportRawStats.PeerUserScore score = scoreMap.computeIfAbsent(recordUserId, key -> {
                StudyReportRawStats.PeerUserScore item = new StudyReportRawStats.PeerUserScore();
                item.setUserId(key);
                return item;
            });
            score.setQuestionCount(score.getQuestionCount() + safeInt(record.getTotalCount()));
            score.setCorrectCount(score.getCorrectCount() + safeInt(record.getCorrectCount()));
        }
        return new ArrayList<>(scoreMap.values());
    }

    private Set<Long> resolvePeerUserIds(Long userId, UserEntity user) {
        Set<Long> peerUserIds = new HashSet<>();
        if (userId != null) {
            peerUserIds.add(userId);
        }
        if (user == null) {
            return peerUserIds;
        }

        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.select("user_id");
        if (StringUtils.isNotBlank(user.getGradeShort())) {
            wrapper.eq("grade_short", StringUtils.trim(user.getGradeShort()));
        } else if (StringUtils.isNotBlank(user.getGrade())) {
            wrapper.eq("grade", StringUtils.trim(user.getGrade()));
        } else {
            return peerUserIds;
        }

        List<UserEntity> peers = iUserService.list(wrapper);
        if (peers == null || peers.isEmpty()) {
            return peerUserIds;
        }
        return peers.stream()
                .map(UserEntity::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
