package com.sqx.app.base.practice.constants;

/**
 * 练习提交来源模式（与 uniapp practiceSource.mode 对齐）
 */
public interface PracticeSourceMode {

    /** 章节全部题目 */
    String ALL = "all";

    /** 错题重做 */
    String WRONG = "wrong";

    /** 单题预览 */
    String SINGLE = "single";

    /** 未传时的默认值 */
    String DEFAULT = ALL;
}
