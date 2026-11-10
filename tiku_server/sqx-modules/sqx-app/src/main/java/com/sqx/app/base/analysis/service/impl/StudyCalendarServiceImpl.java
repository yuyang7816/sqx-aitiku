package com.sqx.app.base.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.analysis.domain.vo.AppStudyCalendarVO;
import com.sqx.app.base.analysis.domain.vo.AppStudyCheckInVO;
import com.sqx.app.base.analysis.service.StudyCalendarService;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.entity.UserStudyCheckIn;
import com.sqx.system.db.service.IUserPracticeRecordService;
import com.sqx.system.db.service.IUserStudyCheckInService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 学习日历：合并练习提交日与手动打卡日
 */
@Service
public class StudyCalendarServiceImpl implements StudyCalendarService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Resource
    private IUserStudyCheckInService iUserStudyCheckInService;

    @Override
    public AppStudyCalendarVO getCalendar(Long userId, YearMonth month) {
        YearMonth targetMonth = month == null ? YearMonth.now() : month;
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        Set<LocalDate> activeDates = new TreeSet<>();
        activeDates.addAll(listPracticeDates(userId, monthStart, monthEnd));
        activeDates.addAll(listCheckInDates(userId, monthStart, monthEnd));

        LocalDate today = LocalDate.now();
        AppStudyCalendarVO vo = new AppStudyCalendarVO();
        vo.setMonth(targetMonth.format(MONTH_FORMAT));
        vo.setActiveDates(activeDates.stream()
                .map(date -> date.format(DATE_FORMAT))
                .collect(Collectors.toList()));
        vo.setMonthStudyDays(activeDates.size());
        vo.setTodayChecked(activeDates.contains(today));
        vo.setStreakDays(calculateStreak(userId, today));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppStudyCheckInVO checkIn(Long userId) {
        if (userId == null) {
            throw new BizException("用户未登录");
        }
        LocalDate today = LocalDate.now();
        UserStudyCheckIn existing = findCheckIn(userId, today);
        boolean newlyChecked = existing == null;
        if (newlyChecked) {
            UserStudyCheckIn record = new UserStudyCheckIn();
            record.setUserId(userId);
            record.setCheckDate(today);
            iUserStudyCheckInService.save(record);
        }

        AppStudyCheckInVO vo = new AppStudyCheckInVO();
        vo.setCheckDate(today.format(DATE_FORMAT));
        vo.setNewlyChecked(newlyChecked);
        vo.setStreakDays(calculateStreak(userId, today));
        return vo;
    }

    Set<LocalDate> listPracticeDates(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPracticeRecord::getUserId, userId)
                .ge(UserPracticeRecord::getSubmitTime, start.atStartOfDay())
                .le(UserPracticeRecord::getSubmitTime, end.atTime(LocalTime.MAX));
        List<UserPracticeRecord> records = iUserPracticeRecordService.list(wrapper);
        Set<LocalDate> dates = new LinkedHashSet<>();
        for (UserPracticeRecord record : records) {
            if (record.getSubmitTime() != null) {
                dates.add(record.getSubmitTime().toLocalDate());
            }
        }
        return dates;
    }

    Set<LocalDate> listCheckInDates(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<UserStudyCheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStudyCheckIn::getUserId, userId)
                .ge(UserStudyCheckIn::getCheckDate, start)
                .le(UserStudyCheckIn::getCheckDate, end);
        return iUserStudyCheckInService.list(wrapper).stream()
                .map(UserStudyCheckIn::getCheckDate)
                .filter(date -> date != null)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    int calculateStreak(Long userId, LocalDate endDate) {
        Set<LocalDate> allDates = new TreeSet<>();
        LocalDate start = endDate.minusDays(366);
        allDates.addAll(listPracticeDates(userId, start, endDate));
        allDates.addAll(listCheckInDates(userId, start, endDate));

        int streak = 0;
        LocalDate cursor = endDate;
        while (allDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    UserStudyCheckIn findCheckIn(Long userId, LocalDate date) {
        LambdaQueryWrapper<UserStudyCheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStudyCheckIn::getUserId, userId)
                .eq(UserStudyCheckIn::getCheckDate, date)
                .last("LIMIT 1");
        return iUserStudyCheckInService.getOne(wrapper);
    }
}
