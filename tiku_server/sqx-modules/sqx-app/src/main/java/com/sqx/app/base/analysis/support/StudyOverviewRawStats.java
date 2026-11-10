package com.sqx.app.base.analysis.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 学情概览原始聚合结果（Provider 层，供 Service 映射为 VO）
 */
@Data
public class StudyOverviewRawStats {

    private int weekQuestionCount;
    private int weekCorrectCount;
    private int lastWeekQuestionCount;
    private int lastWeekCorrectCount;
    private List<SubjectAgg> subjectAggs = new ArrayList<>();
    private List<ChapterAgg> chapterAggs = new ArrayList<>();

    @Data
    public static class SubjectAgg {
        private String subject;
        private int totalCount;
        private int correctCount;
    }

    @Data
    public static class ChapterAgg {
        private String chapterCode;
        private String chapterTitle;
        private String subject;
        private int totalCount;
        private int correctCount;
        private int wrongCount;
    }
}
