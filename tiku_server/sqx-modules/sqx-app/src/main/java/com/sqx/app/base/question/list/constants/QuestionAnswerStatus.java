package com.sqx.app.base.question.list.constants;

/**
 * 题目做题状态常量（与 uniapp chapter.vue mock 字段 type/status 对齐）
 */
public interface QuestionAnswerStatus {

    /** 状态类型：已做对 */
    String TYPE_RIGHT = "right";

    /** 状态类型：已做错 */
    String TYPE_WRONG = "wrong";

    /** 状态类型：未做 */
    String TYPE_TODO = "todo";

    /** 展示文案：已做对 */
    String LABEL_RIGHT = "已做对";

    /** 展示文案：已做错 */
    String LABEL_WRONG = "已做错";

    /** 展示文案：未做 */
    String LABEL_TODO = "未做";

    /** 答题记录：答对 */
    int CORRECT_YES = 1;

    /** 答题记录：答错 */
    int CORRECT_NO = 0;
}
