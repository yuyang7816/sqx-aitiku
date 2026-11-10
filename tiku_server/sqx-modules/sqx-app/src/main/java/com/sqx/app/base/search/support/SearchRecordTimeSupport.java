package com.sqx.app.base.search.support;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 搜题记录展示时间格式化（与 uniapp / 消息列表相对时间一致）
 */
public final class SearchRecordTimeSupport {

    private SearchRecordTimeSupport() {
    }

    public static String formatRecordTime(LocalDateTime updateTime) {
        if (updateTime == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        if (updateTime.toLocalDate().equals(now.toLocalDate())) {
            long minutes = Duration.between(updateTime, now).toMinutes();
            if (minutes >= 0 && minutes < 1) {
                return "刚刚";
            }
            if (minutes >= 0 && minutes < 60) {
                return minutes + "分钟前";
            }
            return "今天 " + updateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (updateTime.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "昨天 " + updateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
