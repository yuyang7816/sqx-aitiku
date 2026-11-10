package com.sqx.app.base.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.practice.constants.PracticeSourceMode;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftAnswerBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftSaveBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeProgressQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeRecordQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeSubmitAnswerBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeSubmitBO;
import com.sqx.app.base.practice.domain.vo.AppPracticeDraftSaveResultVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeDraftVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeProgressVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeRecordItemVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeSubmitResultVO;
import com.sqx.app.base.practice.service.PracticeService;
import com.sqx.app.base.practice.support.PracticeDraftSupport;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.list.constants.QuestionAnswerStatus;
import com.sqx.app.base.question.list.constants.QuestionListMode;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.UserPracticeDraft;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserPracticeDraftService;
import com.sqx.system.db.service.IUserPracticeRecordService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户端章节练习进度与提交：基于 tb_user_question_answer 聚合进度，
 * tb_user_practice_record 持久化答卷，tb_user_question_wrong 维护错题/掌握
 */
@Service
public class PracticeServiceImpl implements PracticeService {

    private static final Logger log = LoggerFactory.getLogger(PracticeServiceImpl.class);
    private static final DateTimeFormatter SUBMIT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_SUBJECT = "章节练习";

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @Resource
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Resource
    private IUserPracticeDraftService iUserPracticeDraftService;

    @Resource
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Override
    public AppPracticeProgressVO getChapterProgress(Long userId, AppPracticeProgressQueryBO query) {
        String chapterCode = StringUtils.trim(query.getChapterId());
        QuestionChapter chapter = resolveChapter(chapterCode);
        if (chapter == null) {
            return buildProgressVO(chapterCode, null, 0, 0);
        }
        int totalCount = countChapterQuestions(chapter.getId());
        int doneCount = countAnsweredQuestions(userId, chapter.getId());
        return buildProgressVO(chapterCode, chapter.getTitle(), doneCount, totalCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPracticeSubmitResultVO submitPractice(Long userId, AppPracticeSubmitBO submit) {
        String chapterCode = StringUtils.trim(submit.getChapterId());
        QuestionChapter chapter = resolveChapter(chapterCode);
        if (chapter == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        String sourceMode = normalizeSourceMode(submit.getSourceMode());
        String subject = StringUtils.defaultIfBlank(StringUtils.trim(submit.getSubject()),
                StringUtils.defaultIfBlank(StringUtils.trim(chapter.getSubject()), DEFAULT_SUBJECT));
        int durationSeconds = Math.max(0, submit.getDurationSeconds() == null ? 0 : submit.getDurationSeconds());

        Map<String, AppPracticeSubmitAnswerBO> dedupAnswers = dedupAnswers(submit.getAnswers());
        List<GradedAnswer> gradedAnswers = new ArrayList<>();
        for (AppPracticeSubmitAnswerBO item : dedupAnswers.values()) {
            Question question = resolveQuestion(item.getQuestionCode());
            if (question == null || !Objects.equals(question.getChapterId(), chapter.getId())) {
                throw new BizException("题目不存在或不属于当前章节：" + item.getQuestionCode());
            }
            String normalizedAnswer = StringUtils.trim(item.getUserAnswer());
            int isCorrect = resolveIsCorrect(question.getRightAnswer(), normalizedAnswer);
            upsertAnswer(userId, question, normalizedAnswer, isCorrect);
            gradedAnswers.add(new GradedAnswer(question, normalizedAnswer, isCorrect));
        }

        int totalCount = gradedAnswers.size();
        int correctCount = (int) gradedAnswers.stream()
                .filter(item -> item.isCorrect == QuestionAnswerStatus.CORRECT_YES)
                .count();
        int wrongCount = totalCount - correctCount;
        int score = calcScore(correctCount, totalCount);

        syncWrongNotebook(userId, subject, gradedAnswers);

        UserPracticeRecord record = savePracticeRecord(
                userId, chapter, sourceMode, subject, totalCount, correctCount, wrongCount, score, durationSeconds);

        clearPracticeDraft(userId, chapterCode, submit.getSourceMode());

        AppPracticeSubmitResultVO result = new AppPracticeSubmitResultVO();
        result.setRecordCode(record.getRecordCode());
        result.setSavedCount(totalCount);
        result.setTotalCount(totalCount);
        result.setCorrectCount(correctCount);
        result.setWrongCount(wrongCount);
        result.setScore(score);
        result.setDurationSeconds(durationSeconds);
        result.setSourceMode(sourceMode);
        result.setProgress(getChapterProgress(userId, buildProgressQuery(chapterCode)));
        return result;
    }

    @Override
    public IPage<AppPracticeRecordItemVO> pagePracticeRecords(Long userId, AppPracticeRecordQueryBO query) {
        AppPracticeRecordQueryBO safeQuery = query == null ? new AppPracticeRecordQueryBO() : query;
        Page<UserPracticeRecord> page = PageUtil.initPage(safeQuery.getPage(), safeQuery.getLimit());

        LambdaQueryWrapper<UserPracticeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPracticeRecord::getUserId, userId);
        if (StringUtils.isNotBlank(safeQuery.getChapterId())) {
            wrapper.eq(UserPracticeRecord::getChapterCode, StringUtils.trim(safeQuery.getChapterId()));
        }
        if (StringUtils.isNotBlank(safeQuery.getSourceMode())) {
            wrapper.eq(UserPracticeRecord::getSourceMode, normalizeSourceMode(safeQuery.getSourceMode()));
        }
        wrapper.orderByDesc(UserPracticeRecord::getSubmitTime);

        IPage<UserPracticeRecord> entityPage = iUserPracticeRecordService.page(page, wrapper);
        List<AppPracticeRecordItemVO> records = entityPage.getRecords().stream()
                .map(this::toRecordItemVO)
                .collect(Collectors.toList());
        return PageUtil.copyPage(entityPage, records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPracticeDraftSaveResultVO savePracticeDraft(Long userId, AppPracticeDraftSaveBO save) {
        String scopeKey = PracticeDraftSupport.resolveScopeKey(save);
        String chapterCode = StringUtils.trimToEmpty(save.getChapterId());
        String paperCode = StringUtils.trimToEmpty(save.getPaperId());
        String sourceMode = PracticeDraftSupport.normalizeSourceMode(save.getSourceMode());
        String subject = StringUtils.defaultIfBlank(StringUtils.trim(save.getSubject()), DEFAULT_SUBJECT);
        int currentIndex = Math.max(0, save.getCurrentIndex() == null ? 0 : save.getCurrentIndex());
        int durationSeconds = Math.max(0, save.getDurationSeconds() == null ? 0 : save.getDurationSeconds());

        Map<String, AppPracticeDraftAnswerBO> dedupAnswers = PracticeDraftSupport.dedupAnswers(save.getAnswers());
        List<AppPracticeDraftAnswerBO> storedAnswers = PracticeDraftSupport.filterStoredAnswers(dedupAnswers);
        LocalDateTime now = LocalDateTime.now();

        UserPracticeDraft existing = findDraftByScope(userId, scopeKey);
        if (existing != null) {
            existing.setChapterCode(chapterCode);
            existing.setPaperCode(paperCode);
            existing.setSourceMode(sourceMode);
            existing.setSubject(subject);
            existing.setCurrentIndex(currentIndex);
            existing.setDurationSeconds(durationSeconds);
            existing.setAnswersJson(PracticeDraftSupport.serializeAnswers(storedAnswers));
            existing.setSavedAt(now);
            iUserPracticeDraftService.updateById(existing);
        } else {
            UserPracticeDraft draft = new UserPracticeDraft();
            draft.setUserId(userId);
            draft.setScopeKey(scopeKey);
            draft.setChapterCode(chapterCode);
            draft.setPaperCode(paperCode);
            draft.setSourceMode(sourceMode);
            draft.setSubject(subject);
            draft.setCurrentIndex(currentIndex);
            draft.setDurationSeconds(durationSeconds);
            draft.setAnswersJson(PracticeDraftSupport.serializeAnswers(storedAnswers));
            draft.setSavedAt(now);
            iUserPracticeDraftService.save(draft);
        }

        AppPracticeDraftSaveResultVO result = new AppPracticeDraftSaveResultVO();
        result.setSaved(true);
        result.setAnswerCount(storedAnswers.size());
        result.setSavedAt(now.format(SUBMIT_TIME_FORMATTER));
        return result;
    }

    @Override
    public AppPracticeDraftVO getPracticeDraft(Long userId, AppPracticeDraftQueryBO query) {
        AppPracticeDraftQueryBO safeQuery = query == null ? new AppPracticeDraftQueryBO() : query;
        String scopeKey = PracticeDraftSupport.resolveScopeKey(safeQuery);
        UserPracticeDraft draft = findDraftByScope(userId, scopeKey);
        if (draft == null) {
            return AppPracticeDraftVO.empty();
        }
        return toDraftVO(draft);
    }

    void clearPracticeDraft(Long userId, String chapterCode, String sourceMode) {
        if (userId == null || StringUtils.isBlank(chapterCode)) {
            return;
        }
        AppPracticeDraftQueryBO query = new AppPracticeDraftQueryBO();
        query.setChapterId(chapterCode);
        query.setSourceMode(sourceMode);
        try {
            String scopeKey = PracticeDraftSupport.resolveScopeKey(query);
            LambdaQueryWrapper<UserPracticeDraft> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserPracticeDraft::getUserId, userId)
                    .eq(UserPracticeDraft::getScopeKey, scopeKey);
            iUserPracticeDraftService.remove(wrapper);
        } catch (BizException ignored) {
            // 无有效 scope 时跳过清理
        } catch (Exception e) {
            // 暂存表未迁移或清理失败时不影响交卷主流程
            log.warn("clear practice draft skipped, userId={}, chapterId={}", userId, chapterCode, e);
        }
    }

    UserPracticeDraft findDraftByScope(Long userId, String scopeKey) {
        LambdaQueryWrapper<UserPracticeDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPracticeDraft::getUserId, userId)
                .eq(UserPracticeDraft::getScopeKey, scopeKey)
                .last("LIMIT 1");
        return iUserPracticeDraftService.getOne(wrapper);
    }

    AppPracticeDraftVO toDraftVO(UserPracticeDraft draft) {
        AppPracticeDraftVO vo = new AppPracticeDraftVO();
        vo.setHasDraft(true);
        vo.setChapterId(StringUtils.defaultString(draft.getChapterCode()));
        vo.setPaperId(StringUtils.defaultString(draft.getPaperCode()));
        vo.setSourceMode(StringUtils.defaultIfBlank(draft.getSourceMode(), PracticeSourceMode.DEFAULT));
        vo.setSubject(StringUtils.defaultIfBlank(draft.getSubject(), DEFAULT_SUBJECT));
        vo.setCurrentIndex(Math.max(0, draft.getCurrentIndex() == null ? 0 : draft.getCurrentIndex()));
        vo.setDurationSeconds(Math.max(0, draft.getDurationSeconds() == null ? 0 : draft.getDurationSeconds()));
        vo.setAnswers(PracticeDraftSupport.parseAnswers(draft.getAnswersJson()));
        if (StringUtils.startsWith(draft.getScopeKey(), "single:")) {
            vo.setQuestionId(StringUtils.substring(draft.getScopeKey(), "single:".length()));
        }
        if (draft.getSavedAt() != null) {
            vo.setSavedAt(draft.getSavedAt().format(SUBMIT_TIME_FORMATTER));
        }
        return vo;
    }

    void syncWrongNotebook(Long userId, String subject, List<GradedAnswer> gradedAnswers) {
        LocalDateTime now = LocalDateTime.now();
        for (GradedAnswer graded : gradedAnswers) {
            if (graded.isCorrect == QuestionAnswerStatus.CORRECT_YES) {
                markWrongMastered(userId, graded.question.getId(), now);
            } else {
                upsertWrongEntry(userId, graded.question.getId(), subject, graded.userAnswer, now);
            }
        }
    }

    void markWrongMastered(Long userId, Long questionId, LocalDateTime now) {
        UserQuestionWrong existing = findWrongEntry(userId, questionId);
        if (existing == null) {
            return;
        }
        existing.setMastered(UserQuestionWrong.MASTERED_YES);
        existing.setLastPracticeAt(now);
        iUserQuestionWrongService.updateById(existing);
    }

    void upsertWrongEntry(Long userId, Long questionId, String subject, String userAnswer, LocalDateTime now) {
        UserQuestionWrong existing = findWrongEntry(userId, questionId);
        if (existing != null) {
            existing.setSubject(subject);
            existing.setUserAnswer(userAnswer);
            existing.setWrongCount(Math.max(1, existing.getWrongCount() == null ? 1 : existing.getWrongCount()) + 1);
            existing.setMastered(UserQuestionWrong.MASTERED_NO);
            existing.setLastPracticeAt(now);
            iUserQuestionWrongService.updateById(existing);
            return;
        }

        UserQuestionWrong wrong = new UserQuestionWrong();
        wrong.setUserId(userId);
        wrong.setQuestionId(questionId);
        wrong.setSubject(subject);
        wrong.setUserAnswer(userAnswer);
        wrong.setWrongCount(1);
        wrong.setMastered(UserQuestionWrong.MASTERED_NO);
        wrong.setLastPracticeAt(now);
        iUserQuestionWrongService.save(wrong);
    }

    UserQuestionWrong findWrongEntry(Long userId, Long questionId) {
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId)
                .eq(UserQuestionWrong::getQuestionId, questionId)
                .last("LIMIT 1");
        return iUserQuestionWrongService.getOne(wrapper);
    }

    UserPracticeRecord savePracticeRecord(Long userId,
                                          QuestionChapter chapter,
                                          String sourceMode,
                                          String subject,
                                          int totalCount,
                                          int correctCount,
                                          int wrongCount,
                                          int score,
                                          int durationSeconds) {
        LocalDateTime now = LocalDateTime.now();
        UserPracticeRecord record = new UserPracticeRecord();
        record.setRecordCode(genRecordCode(userId));
        record.setUserId(userId);
        record.setChapterCode(chapter.getChapterCode());
        record.setChapterTitle(chapter.getTitle());
        record.setSourceMode(sourceMode);
        record.setSubject(subject);
        record.setTotalCount(totalCount);
        record.setCorrectCount(correctCount);
        record.setWrongCount(wrongCount);
        record.setScore(score);
        record.setDurationSeconds(durationSeconds);
        record.setSubmitTime(now);
        iUserPracticeRecordService.save(record);
        return record;
    }

    String genRecordCode(Long userId) {
        return "pr_" + System.currentTimeMillis() + String.format("%04d", userId == null ? 0 : userId % 10000);
    }

    String normalizeSourceMode(String sourceMode) {
        String value = StringUtils.trimToEmpty(sourceMode);
        if (QuestionListMode.VALID_MODES.contains(value)) {
            return value;
        }
        return PracticeSourceMode.DEFAULT;
    }

    int calcScore(int correctCount, int totalCount) {
        if (totalCount <= 0 || correctCount <= 0) {
            return 0;
        }
        return (int) Math.round(correctCount * 100.0 / totalCount);
    }

    AppPracticeRecordItemVO toRecordItemVO(UserPracticeRecord record) {
        AppPracticeRecordItemVO vo = new AppPracticeRecordItemVO();
        vo.setRecordCode(record.getRecordCode());
        vo.setChapterId(record.getChapterCode());
        vo.setChapterTitle(StringUtils.defaultString(record.getChapterTitle()));
        vo.setSourceMode(StringUtils.defaultIfBlank(record.getSourceMode(), PracticeSourceMode.DEFAULT));
        vo.setSubject(StringUtils.defaultIfBlank(record.getSubject(), DEFAULT_SUBJECT));
        vo.setTotalCount(Math.max(0, record.getTotalCount() == null ? 0 : record.getTotalCount()));
        vo.setCorrectCount(Math.max(0, record.getCorrectCount() == null ? 0 : record.getCorrectCount()));
        vo.setWrongCount(Math.max(0, record.getWrongCount() == null ? 0 : record.getWrongCount()));
        vo.setScore(Math.max(0, record.getScore() == null ? 0 : record.getScore()));
        vo.setDurationSeconds(Math.max(0, record.getDurationSeconds() == null ? 0 : record.getDurationSeconds()));
        if (record.getSubmitTime() != null) {
            vo.setSubmitTime(record.getSubmitTime().format(SUBMIT_TIME_FORMATTER));
        }
        return vo;
    }

    QuestionChapter resolveChapter(String chapterId) {
        if (StringUtils.isBlank(chapterId)) {
            return null;
        }
        LambdaQueryWrapper<QuestionChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionChapter::getChapterCode, StringUtils.trim(chapterId))
                .eq(QuestionChapter::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionChapterService.getOne(wrapper);
    }

    Question resolveQuestion(String questionCode) {
        if (StringUtils.isBlank(questionCode)) {
            return null;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuestionCode, StringUtils.trim(questionCode))
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionService.getOne(wrapper);
    }

    int countChapterQuestions(Long chapterId) {
        if (chapterId == null) {
            return 0;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getChapterId, chapterId)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        return Math.toIntExact(iQuestionService.count(wrapper));
    }

    int countAnsweredQuestions(Long userId, Long chapterId) {
        if (userId == null || chapterId == null) {
            return 0;
        }
        List<Long> questionIds = listChapterQuestionIds(chapterId);
        if (questionIds.isEmpty()) {
            return 0;
        }
        LambdaQueryWrapper<UserQuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionAnswer::getUserId, userId)
                .in(UserQuestionAnswer::getQuestionId, questionIds);
        return Math.toIntExact(iUserQuestionAnswerService.count(wrapper));
    }

    List<Long> listChapterQuestionIds(Long chapterId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Question::getId)
                .eq(Question::getChapterId, chapterId)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        List<Question> questions = iQuestionService.list(wrapper);
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }
        return questions.stream().map(Question::getId).collect(Collectors.toList());
    }

    void upsertAnswer(Long userId, Question question, String userAnswer, int isCorrect) {
        LambdaQueryWrapper<UserQuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionAnswer::getUserId, userId)
                .eq(UserQuestionAnswer::getQuestionId, question.getId())
                .last("LIMIT 1");
        UserQuestionAnswer existing = iUserQuestionAnswerService.getOne(wrapper);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            existing.setUserAnswer(userAnswer);
            existing.setIsCorrect(isCorrect);
            existing.setAnswerTime(now);
            iUserQuestionAnswerService.updateById(existing);
            return;
        }

        UserQuestionAnswer answer = new UserQuestionAnswer();
        answer.setUserId(userId);
        answer.setQuestionId(question.getId());
        answer.setUserAnswer(userAnswer);
        answer.setIsCorrect(isCorrect);
        answer.setAnswerTime(now);
        iUserQuestionAnswerService.save(answer);
    }

    int resolveIsCorrect(String rightAnswer, String userAnswer) {
        if (StringUtils.isBlank(rightAnswer) || StringUtils.isBlank(userAnswer)) {
            return QuestionAnswerStatus.CORRECT_NO;
        }
        return StringUtils.equalsIgnoreCase(StringUtils.trim(rightAnswer), StringUtils.trim(userAnswer))
                ? QuestionAnswerStatus.CORRECT_YES
                : QuestionAnswerStatus.CORRECT_NO;
    }

    Map<String, AppPracticeSubmitAnswerBO> dedupAnswers(List<AppPracticeSubmitAnswerBO> answers) {
        Map<String, AppPracticeSubmitAnswerBO> dedup = new LinkedHashMap<>();
        if (answers == null) {
            return dedup;
        }
        for (AppPracticeSubmitAnswerBO item : answers) {
            if (item == null || StringUtils.isBlank(item.getQuestionCode())) {
                continue;
            }
            dedup.put(StringUtils.trim(item.getQuestionCode()), item);
        }
        return dedup;
    }

    AppPracticeProgressVO buildProgressVO(String chapterId, String chapterTitle, int doneCount, int totalCount) {
        AppPracticeProgressVO vo = new AppPracticeProgressVO();
        vo.setChapterId(chapterId);
        vo.setChapterTitle(chapterTitle);
        vo.setDoneCount(Math.max(0, doneCount));
        vo.setTotalCount(Math.max(0, totalCount));
        vo.setPercent(calcPercent(doneCount, totalCount));
        return vo;
    }

    int calcPercent(int doneCount, int totalCount) {
        if (totalCount <= 0 || doneCount <= 0) {
            return 0;
        }
        return (int) Math.round(doneCount * 100.0 / totalCount);
    }

    AppPracticeProgressQueryBO buildProgressQuery(String chapterId) {
        AppPracticeProgressQueryBO query = new AppPracticeProgressQueryBO();
        query.setChapterId(chapterId);
        return query;
    }

    static final class GradedAnswer {
        private final Question question;
        private final String userAnswer;
        private final int isCorrect;

        GradedAnswer(Question question, String userAnswer, int isCorrect) {
            this.question = question;
            this.userAnswer = userAnswer;
            this.isCorrect = isCorrect;
        }
    }
}
