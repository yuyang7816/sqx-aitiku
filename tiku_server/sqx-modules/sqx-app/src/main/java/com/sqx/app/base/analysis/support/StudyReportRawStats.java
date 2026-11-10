package com.sqx.app.base.analysis.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 月度报告原始聚合结果（Provider 层，供 Service 映射为 VO）
 */
@Data
public class StudyReportRawStats {

    private int monthQuestionCount;
    private int monthCorrectCount;
    private int lastMonthQuestionCount;
    private int lastMonthCorrectCount;
    private String grade;
    private String gradeShort;
    private int staleWrongCount;
    private List<SubjectAgg> subjectAggs = new ArrayList<>();
    private List<ChapterAgg> chapterAggs = new ArrayList<>();
    private List<PeerUserScore> peerScores = new ArrayList<>();

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

    @Data
    public static class PeerUserScore {
        private Long userId;
        private int questionCount;
        private int correctCount;
    }
}
