package com.sqx.app.base.user.stats.support;

import lombok.Data;

/**
 * 用户学习统计原始聚合结果（基于 tb_user_practice_record）
 */
@Data
public class UserStudyRawStats {

    /** 累计学习天数（有提交记录的自然日去重） */
    private int studyDays;

    /** 累计做题总数 */
    private int questionCount;

    /** 练习提交次数 */
    private int submitCount;

    /** 是否存在单次练习满分（score=100） */
    private boolean hasPerfectScore;
}
