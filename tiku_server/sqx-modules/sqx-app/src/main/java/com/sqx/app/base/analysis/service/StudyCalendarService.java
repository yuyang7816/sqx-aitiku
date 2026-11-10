package com.sqx.app.base.analysis.service;

import com.sqx.app.base.analysis.domain.vo.AppStudyCalendarVO;
import com.sqx.app.base.analysis.domain.vo.AppStudyCheckInVO;

import java.time.YearMonth;

/**
 * 学习日历：练习记录 + 手动打卡
 */
public interface StudyCalendarService {

    AppStudyCalendarVO getCalendar(Long userId, YearMonth month);

    AppStudyCheckInVO checkIn(Long userId);
}
