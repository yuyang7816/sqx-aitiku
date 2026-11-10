package com.sqx.app.base.question.manage.constants;

/**
 * 刷题中心练习入口（与 uniapp practice-config 四种模式对应）
 */
public final class QuestionPracticeEntry {

    public static final String CHAPTER = "chapter";
    public static final String KNOWLEDGE = "knowledge";
    public static final String SPECIAL = "special";
    public static final String EXAM = "exam";

    private QuestionPracticeEntry() {
    }

    public static String labelOf(String entry) {
        if (CHAPTER.equals(entry)) {
            return "章节练习";
        }
        if (KNOWLEDGE.equals(entry)) {
            return "知识点练习";
        }
        if (SPECIAL.equals(entry)) {
            return "专项训练";
        }
        if (EXAM.equals(entry)) {
            return "真题模拟";
        }
        return "—";
    }

    public static boolean isPaperEntry(String entry) {
        return SPECIAL.equals(entry) || EXAM.equals(entry);
    }
}
