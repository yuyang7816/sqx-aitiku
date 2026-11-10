package com.sqx.app.base.analysis.support;

import lombok.Data;

/**
 * 今日练习统计原始聚合结果（供 Service 映射为 VO）
 */
@Data
public class StudyTodayRawStats {

    /** 今日累计练习时长（分钟） */
    private int durationMinutes;

    /** 今日已做题目数量 */
    private int questionCount;
}
