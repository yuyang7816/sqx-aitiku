package com.sqx.app.base.analysis.service;

import java.time.YearMonth;

/**
 * 月度学习报告站内消息推送
 */
public interface MonthlyReportPushService {

    /**
     * 为指定自然月内有练习记录的用户推送 notice-report 消息
     *
     * @param month 报告所属自然月
     * @return 实际推送条数（已推送过的用户不计入）
     */
    int pushForMonth(YearMonth month);

    /**
     * 向单个用户推送指定月份的月报通知
     *
     * @param userId 用户 id
     * @param month  报告所属自然月
     * @return 是否新推送（false 表示该月已推送过）
     */
    boolean pushToUser(Long userId, YearMonth month);
}
