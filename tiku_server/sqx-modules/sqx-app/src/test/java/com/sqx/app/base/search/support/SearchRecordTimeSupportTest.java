package com.sqx.app.base.search.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchRecordTimeSupportTest {

    @Test
    void formatRecordTime_shouldReturnJustNowForRecentRecord() {
        String time = SearchRecordTimeSupport.formatRecordTime(LocalDateTime.now());
        assertEquals("刚刚", time);
    }

    @Test
    void formatRecordTime_shouldReturnTodayPrefix() {
        // 固定使用当日 10:00，避免凌晨跑测时 minusHours 跨天导致误判为「昨天」
        LocalDateTime todayMorning = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);
        String time = SearchRecordTimeSupport.formatRecordTime(todayMorning);
        assertTrue(time.startsWith("今天 10:00") || time.endsWith("分钟前") || time.equals("刚刚"));
    }

    @Test
    void formatRecordTime_shouldReturnYesterdayPrefix() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(15).withMinute(45);
        String time = SearchRecordTimeSupport.formatRecordTime(yesterday);
        assertTrue(time.startsWith("昨天 "));
    }
}
