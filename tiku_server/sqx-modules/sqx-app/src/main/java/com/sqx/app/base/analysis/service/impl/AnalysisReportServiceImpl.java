package com.sqx.app.base.analysis.service.impl;

import com.sqx.app.base.analysis.domain.vo.AppAnalysisPeerComparisonVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportAdviceVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportSubjectBarVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.service.AnalysisReportService;
import com.sqx.app.base.analysis.support.StudyReportRawStats;
import com.sqx.app.base.analysis.support.StudyReportStatsProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 月度学习报告：经 {@link StudyReportStatsProvider} 聚合练习记录并生成排名、柱状图与 AI 建议
 */
@Service
public class AnalysisReportServiceImpl implements AnalysisReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_TITLE_FORMAT = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA);
    private static final int MIN_CHAPTER_QUESTIONS = 1;
    private static final int ADVICE_LIMIT = 3;
    private static final int DEFAULT_BEAT_PERCENT = 50;

    @Resource
    private StudyReportStatsProvider studyReportStatsProvider;

    @Override
    public AppAnalysisReportVO getReport(Long userId, YearMonth month) {
        YearMonth targetMonth = month == null ? YearMonth.now() : month;
        StudyReportRawStats raw = studyReportStatsProvider.aggregateReport(userId, targetMonth);

        AppAnalysisReportVO vo = new AppAnalysisReportVO();
        vo.setMonth(targetMonth.toString());
        vo.setMonthTitle(targetMonth.format(MONTH_TITLE_FORMAT) + " · 学习报告");
        vo.setMonthStartDate(targetMonth.atDay(1).format(DATE_FORMAT));
        vo.setMonthEndDate(targetMonth.atEndOfMonth().format(DATE_FORMAT));
        vo.setMonthQuestionCount(Math.max(0, raw.getMonthQuestionCount()));
        vo.setCorrectRate(calcCorrectRate(raw.getMonthCorrectCount(), raw.getMonthQuestionCount()));
        vo.setMonthQuestionChangePercent(calcRelativeChangePercent(
                raw.getMonthQuestionCount(), raw.getLastMonthQuestionCount()));

        AppAnalysisPeerComparisonVO peerComparison = buildPeerComparison(userId, raw);
        vo.setPeerComparison(peerComparison);
        vo.setRankPercent(peerComparison.getBeatPercent());
        vo.setRankTitle(buildRankTitle(peerComparison.getBeatPercent()));
        vo.setRankSubtitle(buildRankSubtitle(raw));
        vo.setSubjectBars(buildSubjectBars(raw.getSubjectAggs()));
        vo.setAiAdvices(buildAiAdvices(raw));
        return vo;
    }

    private AppAnalysisPeerComparisonVO buildPeerComparison(Long userId, StudyReportRawStats raw) {
        AppAnalysisPeerComparisonVO peer = new AppAnalysisPeerComparisonVO();
        peer.setGradeLabel(resolveGradeLabel(raw));
        peer.setUserCorrectRate(calcCorrectRate(raw.getMonthCorrectCount(), raw.getMonthQuestionCount()));

        List<StudyReportRawStats.PeerUserScore> peerScores = raw.getPeerScores();
        if (peerScores == null || peerScores.isEmpty()) {
            peer.setBeatPercent(DEFAULT_BEAT_PERCENT);
            peer.setPeerSampleCount(1);
            peer.setPeerAvgCorrectRate(peer.getUserCorrectRate());
            return peer;
        }

        int userScore = calcCompositeScore(raw.getMonthCorrectCount(), raw.getMonthQuestionCount());
        int beatCount = 0;
        int peerCountExcludingSelf = 0;
        int totalCorrect = 0;
        int totalQuestions = 0;
        for (StudyReportRawStats.PeerUserScore score : peerScores) {
            totalCorrect += score.getCorrectCount();
            totalQuestions += score.getQuestionCount();
            if (userId.equals(score.getUserId())) {
                continue;
            }
            peerCountExcludingSelf++;
            int peerComposite = calcCompositeScore(score.getCorrectCount(), score.getQuestionCount());
            if (userScore > peerComposite) {
                beatCount++;
            }
        }

        int beatPercent;
        if (peerCountExcludingSelf <= 0) {
            beatPercent = userScore > 0 ? Math.max(DEFAULT_BEAT_PERCENT, peer.getUserCorrectRate()) : DEFAULT_BEAT_PERCENT;
        } else {
            beatPercent = Math.min(99, Math.max(1, Math.round(beatCount * 100f / peerCountExcludingSelf)));
        }

        peer.setBeatPercent(beatPercent);
        peer.setPeerSampleCount(Math.max(1, peerScores.size()));
        peer.setPeerAvgCorrectRate(calcCorrectRate(totalCorrect, totalQuestions));
        return peer;
    }

    private int calcCompositeScore(int correctCount, int questionCount) {
        if (questionCount <= 0) {
            return 0;
        }
        return calcCorrectRate(correctCount, questionCount) * 100 + Math.min(questionCount, 999);
    }

    private String resolveGradeLabel(StudyReportRawStats raw) {
        if (StringUtils.isNotBlank(raw.getGradeShort())) {
            return StringUtils.trim(raw.getGradeShort());
        }
        if (StringUtils.isNotBlank(raw.getGrade())) {
            return StringUtils.trim(raw.getGrade());
        }
        return "同龄人";
    }

    private String buildRankTitle(Integer beatPercent) {
        int percent = beatPercent == null ? DEFAULT_BEAT_PERCENT : Math.max(0, Math.min(100, beatPercent));
        return "超越了 " + percent + "% 的同龄人";
    }

    private String buildRankSubtitle(StudyReportRawStats raw) {
        List<String> highlightSubjects = raw.getSubjectAggs() == null ? new ArrayList<>() : raw.getSubjectAggs().stream()
                .filter(agg -> agg.getTotalCount() >= MIN_CHAPTER_QUESTIONS)
                .sorted(Comparator
                        .comparingInt((StudyReportRawStats.SubjectAgg agg) ->
                                calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount()))
                        .reversed())
                .map(StudyReportRawStats.SubjectAgg::getSubject)
                .limit(2)
                .collect(Collectors.toList());

        if (highlightSubjects.size() >= 2) {
            return "你在" + highlightSubjects.get(0) + "和" + highlightSubjects.get(1) + "方面的进步非常显著";
        }
        if (highlightSubjects.size() == 1) {
            return "你在" + highlightSubjects.get(0) + "方面表现突出，继续保持";
        }
        if (raw.getMonthQuestionCount() > 0) {
            return "本月已完成 " + raw.getMonthQuestionCount() + " 道题，坚持就是进步";
        }
        return "本月尚未开始练习，现在出发也不晚";
    }

    private List<AppAnalysisReportSubjectBarVO> buildSubjectBars(List<StudyReportRawStats.SubjectAgg> subjectAggs) {
        List<AppAnalysisReportSubjectBarVO> bars = new ArrayList<>();
        if (subjectAggs == null) {
            return bars;
        }
        subjectAggs.stream()
                .sorted(Comparator.comparing(StudyReportRawStats.SubjectAgg::getSubject))
                .forEach(agg -> {
                    int correctRate = calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount());
                    AppAnalysisReportSubjectBarVO bar = new AppAnalysisReportSubjectBarVO();
                    bar.setLabel(agg.getSubject());
                    bar.setCorrectRate(correctRate);
                    bar.setPercent(correctRate + "%");
                    bars.add(bar);
                });
        return bars;
    }

    private List<AppAnalysisReportAdviceVO> buildAiAdvices(StudyReportRawStats raw) {
        List<AppAnalysisReportAdviceVO> advices = new ArrayList<>();

        StudyReportRawStats.ChapterAgg weakestChapter = findWeakestChapter(raw.getChapterAggs());
        if (weakestChapter != null) {
            int chapterRate = calcCorrectRate(weakestChapter.getCorrectCount(), weakestChapter.getTotalCount());
            AppAnalysisReportAdviceVO advice = new AppAnalysisReportAdviceVO();
            advice.setOrder(1);
            advice.setContent("夯实基础：「" + weakestChapter.getChapterTitle() + "」正确率仅 "
                    + chapterRate + "%，建议每天做 2 道"
                    + weakestChapter.getSubject() + "专项练习。");
            advice.setAction(resolveChapterAction(weakestChapter));
            advices.add(advice);
        }

        StudyReportRawStats.SubjectAgg weakestEnglish = findWeakestEnglishSubject(raw.getSubjectAggs());
        if (weakestEnglish != null) {
            int englishRate = calcCorrectRate(weakestEnglish.getCorrectCount(), weakestEnglish.getTotalCount());
            AppAnalysisReportAdviceVO advice = new AppAnalysisReportAdviceVO();
            advice.setOrder(advices.size() + 1);
            advice.setContent("词汇积累：英语正确率 " + englishRate
                    + "%，在阅读中常遇到薄弱点，建议利用「学习日历」坚持打卡。");
            advice.setAction("calendar-checkin");
            advices.add(advice);
        }

        if (raw.getStaleWrongCount() > 0) {
            AppAnalysisReportAdviceVO advice = new AppAnalysisReportAdviceVO();
            advice.setOrder(advices.size() + 1);
            advice.setContent("错题重做：错题本中有 " + raw.getStaleWrongCount()
                    + " 道题已经超过 3 天未复习，建议今晚集中重做。");
            advice.setAction("wrong-practice");
            advices.add(advice);
        }

        while (advices.size() < ADVICE_LIMIT) {
            AppAnalysisReportAdviceVO advice = buildFallbackAdvice(advices.size() + 1, raw);
            advices.add(advice);
        }
        return advices.stream().limit(ADVICE_LIMIT).collect(Collectors.toList());
    }

    private AppAnalysisReportAdviceVO buildFallbackAdvice(int order, StudyReportRawStats raw) {
        AppAnalysisReportAdviceVO advice = new AppAnalysisReportAdviceVO();
        advice.setOrder(order);
        if (order == 1) {
            advice.setContent("保持节奏：建议每天固定 30 分钟刷题，稳步提升正确率。");
            advice.setAction("ocr-practice");
            return advice;
        }
        if (order == 2) {
            advice.setContent("查漏补缺：针对本月错题进行专项复盘，避免重复犯错。");
            advice.setAction("wrong-practice");
            return advice;
        }
        advice.setContent("本月已完成 " + Math.max(0, raw.getMonthQuestionCount())
                + " 道题，让 AI 帮你拆一份可执行的复习计划。");
        advice.setAction("analysis-chat");
        return advice;
    }

    private StudyReportRawStats.ChapterAgg findWeakestChapter(List<StudyReportRawStats.ChapterAgg> chapterAggs) {
        if (chapterAggs == null || chapterAggs.isEmpty()) {
            return null;
        }
        return chapterAggs.stream()
                .filter(agg -> agg.getTotalCount() >= MIN_CHAPTER_QUESTIONS)
                .min(Comparator
                        .comparingInt((StudyReportRawStats.ChapterAgg agg) ->
                                calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount()))
                        .thenComparing(Comparator.comparingInt(StudyReportRawStats.ChapterAgg::getTotalCount).reversed()))
                .orElse(null);
    }

    private StudyReportRawStats.SubjectAgg findWeakestEnglishSubject(List<StudyReportRawStats.SubjectAgg> subjectAggs) {
        if (subjectAggs == null || subjectAggs.isEmpty()) {
            return null;
        }
        return subjectAggs.stream()
                .filter(agg -> StringUtils.contains(agg.getSubject(), "英语"))
                .filter(agg -> agg.getTotalCount() >= MIN_CHAPTER_QUESTIONS)
                .min(Comparator.comparingInt((StudyReportRawStats.SubjectAgg agg) ->
                        calcCorrectRate(agg.getCorrectCount(), agg.getTotalCount())))
                .orElse(null);
    }

    private String resolveChapterAction(StudyReportRawStats.ChapterAgg agg) {
        if (agg.getWrongCount() > 0 && agg.getWrongCount() >= agg.getCorrectCount()) {
            return "wrong-practice";
        }
        String subject = StringUtils.defaultString(agg.getSubject());
        if (subject.contains("英语") || subject.contains("语文")) {
            return "analysis-chat";
        }
        return "ocr-practice";
    }

    private int calcCorrectRate(int correctCount, int totalCount) {
        if (totalCount <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(correctCount * 100f / totalCount));
    }

    private int calcRelativeChangePercent(int current, int previous) {
        if (previous <= 0) {
            return current > 0 ? 100 : 0;
        }
        return Math.round((current - previous) * 100f / previous);
    }
}
