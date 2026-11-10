package com.sqx.app.base.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.sqx.app.base.question.list.constants.QuestionAnswerStatus;
import com.sqx.common.core.exception.BizException;
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
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/practice/progress、POST /app/practice/submit、GET /app/practice/records、
 * POST/GET /app/practice/draft 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PracticeServiceImplTest {

    @Mock
    private IQuestionChapterService iQuestionChapterService;

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @Mock
    private IUserPracticeRecordService iUserPracticeRecordService;

    @Mock
    private IUserPracticeDraftService iUserPracticeDraftService;

    @Mock
    private IUserQuestionWrongService iUserQuestionWrongService;

    @InjectMocks
    private PracticeServiceImpl practiceService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, QuestionChapter.class);
        TableInfoHelper.initTableInfo(assistant, Question.class);
        TableInfoHelper.initTableInfo(assistant, UserQuestionAnswer.class);
        TableInfoHelper.initTableInfo(assistant, UserPracticeRecord.class);
        TableInfoHelper.initTableInfo(assistant, UserPracticeDraft.class);
        TableInfoHelper.initTableInfo(assistant, UserQuestionWrong.class);
    }

    @Test
    void getChapterProgress_whenChapterNotFound_shouldReturnZeroProgress() {
        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(null);

        AppPracticeProgressVO result = practiceService.getChapterProgress(100L, buildProgressQuery("unknown"));

        assertEquals("unknown", result.getChapterId());
        assertEquals(0, result.getPercent());
        assertEquals(0, result.getDoneCount());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    void getChapterProgress_shouldReturnPercentAndCounts() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers", "第一章：有理数");
        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.count(any(Wrapper.class))).thenReturn(3L);
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(
                buildQuestion(101L, "rational-numbers-q01", 1L, "A"),
                buildQuestion(102L, "rational-numbers-q02", 1L, "B"),
                buildQuestion(103L, "rational-numbers-q03", 1L, "C")
        ));
        when(iUserQuestionAnswerService.count(any(Wrapper.class))).thenReturn(2L);

        AppPracticeProgressVO result = practiceService.getChapterProgress(100L, buildProgressQuery("rational-numbers"));

        assertEquals("rational-numbers", result.getChapterId());
        assertEquals("第一章：有理数", result.getChapterTitle());
        assertEquals(3, result.getTotalCount());
        assertEquals(2, result.getDoneCount());
        assertEquals(67, result.getPercent());
    }

    @Test
    void submitPractice_whenChapterNotFound_shouldThrow() {
        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(null);

        assertThrows(BizException.class, () -> practiceService.submitPractice(100L, buildSubmit("unknown")));
    }

    @Test
    void submitPractice_shouldGradePersistRecordAndUpdateWrongNotebook() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers", "第一章：有理数");
        Question q1 = buildQuestion(101L, "rational-numbers-q01", 1L, "A");
        Question q2 = buildQuestion(102L, "rational-numbers-q02", 1L, "B");

        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.getOne(any(Wrapper.class)))
                .thenReturn(q1)
                .thenReturn(q2);
        when(iUserQuestionAnswerService.getOne(any(Wrapper.class))).thenReturn(null);
        when(iUserQuestionWrongService.getOne(any(Wrapper.class))).thenReturn(null);
        when(iQuestionService.count(any(Wrapper.class))).thenReturn(3L);
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(q1, q2, buildQuestion(103L, "rational-numbers-q03", 1L, "C")));
        when(iUserQuestionAnswerService.count(any(Wrapper.class))).thenReturn(2L);

        AppPracticeSubmitBO submit = buildSubmit("rational-numbers");
        submit.setDurationSeconds(120);
        submit.setSourceMode("all");
        submit.setSubject("数学 · 初三");

        AppPracticeSubmitResultVO result = practiceService.submitPractice(100L, submit);

        assertEquals(2, result.getSavedCount());
        assertEquals(1, result.getCorrectCount());
        assertEquals(1, result.getWrongCount());
        assertEquals(50, result.getScore());
        assertEquals(120, result.getDurationSeconds());
        assertEquals(PracticeSourceMode.ALL, result.getSourceMode());
        assertNotNull(result.getRecordCode());
        assertNotNull(result.getProgress());
        assertEquals(2, result.getProgress().getDoneCount());

        ArgumentCaptor<UserQuestionAnswer> answerCaptor = ArgumentCaptor.forClass(UserQuestionAnswer.class);
        verify(iUserQuestionAnswerService, times(2)).save(answerCaptor.capture());
        List<UserQuestionAnswer> savedAnswers = answerCaptor.getAllValues();
        assertEquals(QuestionAnswerStatus.CORRECT_YES, savedAnswers.get(0).getIsCorrect().intValue());
        assertEquals(QuestionAnswerStatus.CORRECT_NO, savedAnswers.get(1).getIsCorrect().intValue());

        ArgumentCaptor<UserPracticeRecord> recordCaptor = ArgumentCaptor.forClass(UserPracticeRecord.class);
        verify(iUserPracticeRecordService).save(recordCaptor.capture());
        UserPracticeRecord record = recordCaptor.getValue();
        assertEquals(2, record.getTotalCount().intValue());
        assertEquals(50, record.getScore().intValue());
        assertEquals("数学 · 初三", record.getSubject());

        ArgumentCaptor<UserQuestionWrong> wrongCaptor = ArgumentCaptor.forClass(UserQuestionWrong.class);
        verify(iUserQuestionWrongService).save(wrongCaptor.capture());
        assertEquals(102L, wrongCaptor.getValue().getQuestionId().longValue());
        assertEquals(UserQuestionWrong.MASTERED_NO, wrongCaptor.getValue().getMastered().intValue());

        verify(iUserPracticeDraftService).remove(any(Wrapper.class));
    }

    @Test
    void submitPractice_whenCorrectAgain_shouldMarkWrongMastered() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers", "第一章：有理数");
        Question q1 = buildQuestion(101L, "rational-numbers-q01", 1L, "A");

        UserQuestionWrong existingWrong = new UserQuestionWrong();
        existingWrong.setId(9L);
        existingWrong.setUserId(100L);
        existingWrong.setQuestionId(101L);
        existingWrong.setWrongCount(2);
        existingWrong.setMastered(UserQuestionWrong.MASTERED_NO);

        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(q1);
        when(iUserQuestionAnswerService.getOne(any(Wrapper.class))).thenReturn(null);
        when(iUserQuestionWrongService.getOne(any(Wrapper.class))).thenReturn(existingWrong);
        when(iQuestionService.count(any(Wrapper.class))).thenReturn(1L);
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(q1));
        when(iUserQuestionAnswerService.count(any(Wrapper.class))).thenReturn(1L);

        AppPracticeSubmitBO submit = new AppPracticeSubmitBO();
        submit.setChapterId("rational-numbers");
        AppPracticeSubmitAnswerBO answer = new AppPracticeSubmitAnswerBO();
        answer.setQuestionCode("rational-numbers-q01");
        answer.setUserAnswer("A");
        submit.setAnswers(Collections.singletonList(answer));

        practiceService.submitPractice(100L, submit);

        ArgumentCaptor<UserQuestionWrong> wrongCaptor = ArgumentCaptor.forClass(UserQuestionWrong.class);
        verify(iUserQuestionWrongService).updateById(wrongCaptor.capture());
        assertEquals(UserQuestionWrong.MASTERED_YES, wrongCaptor.getValue().getMastered().intValue());
        verify(iUserQuestionWrongService, never()).save(any(UserQuestionWrong.class));
    }

    @Test
    void submitPractice_whenQuestionNotInChapter_shouldThrow() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers", "第一章：有理数");
        Question otherChapterQuestion = buildQuestion(201L, "other-q01", 2L, "A");

        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(otherChapterQuestion);

        AppPracticeSubmitBO submit = new AppPracticeSubmitBO();
        submit.setChapterId("rational-numbers");
        AppPracticeSubmitAnswerBO answer = new AppPracticeSubmitAnswerBO();
        answer.setQuestionCode("other-q01");
        answer.setUserAnswer("A");
        submit.setAnswers(Collections.singletonList(answer));

        assertThrows(BizException.class, () -> practiceService.submitPractice(100L, submit));
        verify(iUserQuestionAnswerService, never()).save(any(UserQuestionAnswer.class));
        verify(iUserPracticeRecordService, never()).save(any(UserPracticeRecord.class));
    }

    @Test
    void pagePracticeRecords_shouldMapEntityToVo() {
        UserPracticeRecord entity = new UserPracticeRecord();
        entity.setRecordCode("pr_test_001");
        entity.setChapterCode("rational-numbers");
        entity.setChapterTitle("第一章：有理数");
        entity.setSourceMode("all");
        entity.setSubject("数学 · 初三");
        entity.setTotalCount(3);
        entity.setCorrectCount(2);
        entity.setWrongCount(1);
        entity.setScore(67);
        entity.setDurationSeconds(180);
        entity.setSubmitTime(java.time.LocalDateTime.of(2026, 8, 5, 20, 30, 0));

        Page<UserPracticeRecord> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(entity));
        page.setTotal(1);

        when(iUserPracticeRecordService.page(any(Page.class), any(Wrapper.class))).thenReturn(page);

        AppPracticeRecordQueryBO query = new AppPracticeRecordQueryBO();
        query.setPage(1L);
        query.setLimit(10L);
        query.setChapterId("rational-numbers");

        IPage<AppPracticeRecordItemVO> result = practiceService.pagePracticeRecords(100L, query);

        assertEquals(1, result.getTotal());
        AppPracticeRecordItemVO item = result.getRecords().get(0);
        assertEquals("pr_test_001", item.getRecordCode());
        assertEquals("rational-numbers", item.getChapterId());
        assertEquals(67, item.getScore());
        assertEquals(180, item.getDurationSeconds());
    }

    @Test
    void calcPercent_shouldRoundHalfUp() {
        assertEquals(0, practiceService.calcPercent(0, 3));
        assertEquals(0, practiceService.calcPercent(1, 0));
        assertEquals(33, practiceService.calcPercent(1, 3));
        assertEquals(67, practiceService.calcPercent(2, 3));
        assertEquals(100, practiceService.calcPercent(3, 3));
    }

    @Test
    void calcScore_shouldMatchCorrectRate() {
        assertEquals(0, practiceService.calcScore(0, 3));
        assertEquals(67, practiceService.calcScore(2, 3));
        assertEquals(100, practiceService.calcScore(3, 3));
    }

    @Test
    void savePracticeDraft_shouldInsertNewDraft() {
        when(iUserPracticeDraftService.getOne(any(Wrapper.class))).thenReturn(null);

        AppPracticeDraftSaveBO save = buildDraftSave("rational-numbers", 1, "A");
        AppPracticeDraftSaveResultVO result = practiceService.savePracticeDraft(100L, save);

        assertTrue(result.getSaved());
        assertEquals(1, result.getAnswerCount());
        assertNotNull(result.getSavedAt());

        ArgumentCaptor<UserPracticeDraft> draftCaptor = ArgumentCaptor.forClass(UserPracticeDraft.class);
        verify(iUserPracticeDraftService).save(draftCaptor.capture());
        UserPracticeDraft draft = draftCaptor.getValue();
        assertEquals("chapter:rational-numbers:all", draft.getScopeKey());
        assertEquals(1, draft.getCurrentIndex().intValue());
        assertEquals("rational-numbers", draft.getChapterCode());
    }

    @Test
    void savePracticeDraft_whenExists_shouldUpdateDraft() {
        UserPracticeDraft existing = new UserPracticeDraft();
        existing.setId(1L);
        existing.setScopeKey("chapter:rational-numbers:all");
        when(iUserPracticeDraftService.getOne(any(Wrapper.class))).thenReturn(existing);

        AppPracticeDraftSaveBO save = buildDraftSave("rational-numbers", 2, "B");
        practiceService.savePracticeDraft(100L, save);

        ArgumentCaptor<UserPracticeDraft> draftCaptor = ArgumentCaptor.forClass(UserPracticeDraft.class);
        verify(iUserPracticeDraftService).updateById(draftCaptor.capture());
        assertEquals(2, draftCaptor.getValue().getCurrentIndex().intValue());
        verify(iUserPracticeDraftService, never()).save(any(UserPracticeDraft.class));
    }

    @Test
    void getPracticeDraft_whenNotFound_shouldReturnEmpty() {
        when(iUserPracticeDraftService.getOne(any(Wrapper.class))).thenReturn(null);

        AppPracticeDraftQueryBO query = new AppPracticeDraftQueryBO();
        query.setChapterId("rational-numbers");
        AppPracticeDraftVO result = practiceService.getPracticeDraft(100L, query);

        assertFalse(result.getHasDraft());
        assertEquals(0, result.getCurrentIndex());
    }

    @Test
    void getPracticeDraft_shouldRestoreSavedProgress() {
        UserPracticeDraft draft = new UserPracticeDraft();
        draft.setScopeKey("chapter:rational-numbers:all");
        draft.setChapterCode("rational-numbers");
        draft.setSourceMode("all");
        draft.setSubject("数学 · 初三");
        draft.setCurrentIndex(2);
        draft.setDurationSeconds(90);
        draft.setAnswersJson("[{\"questionCode\":\"rational-numbers-q01\",\"userAnswer\":\"A\"}]");
        draft.setSavedAt(java.time.LocalDateTime.of(2026, 8, 5, 21, 0, 0));
        when(iUserPracticeDraftService.getOne(any(Wrapper.class))).thenReturn(draft);

        AppPracticeDraftQueryBO query = new AppPracticeDraftQueryBO();
        query.setChapterId("rational-numbers");
        AppPracticeDraftVO result = practiceService.getPracticeDraft(100L, query);

        assertTrue(result.getHasDraft());
        assertEquals(2, result.getCurrentIndex());
        assertEquals(90, result.getDurationSeconds());
        assertEquals(1, result.getAnswers().size());
        assertEquals("rational-numbers-q01", result.getAnswers().get(0).getQuestionCode());
        assertEquals("A", result.getAnswers().get(0).getUserAnswer());
    }

    private AppPracticeDraftSaveBO buildDraftSave(String chapterId, int currentIndex, String userAnswer) {
        AppPracticeDraftSaveBO save = new AppPracticeDraftSaveBO();
        save.setChapterId(chapterId);
        save.setCurrentIndex(currentIndex);
        save.setDurationSeconds(60);
        save.setSourceMode("all");
        save.setSubject("数学 · 初三");

        AppPracticeDraftAnswerBO answer = new AppPracticeDraftAnswerBO();
        answer.setQuestionCode("rational-numbers-q01");
        answer.setUserAnswer(userAnswer);
        save.setAnswers(Collections.singletonList(answer));
        return save;
    }

    private AppPracticeProgressQueryBO buildProgressQuery(String chapterId) {
        AppPracticeProgressQueryBO query = new AppPracticeProgressQueryBO();
        query.setChapterId(chapterId);
        return query;
    }

    private AppPracticeSubmitBO buildSubmit(String chapterId) {
        AppPracticeSubmitBO submit = new AppPracticeSubmitBO();
        submit.setChapterId(chapterId);

        AppPracticeSubmitAnswerBO a1 = new AppPracticeSubmitAnswerBO();
        a1.setQuestionCode("rational-numbers-q01");
        a1.setUserAnswer("A");

        AppPracticeSubmitAnswerBO a2 = new AppPracticeSubmitAnswerBO();
        a2.setQuestionCode("rational-numbers-q02");
        a2.setUserAnswer("A");

        submit.setAnswers(Arrays.asList(a1, a2));
        return submit;
    }

    private QuestionChapter buildChapter(Long id, String code, String title) {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(id);
        chapter.setChapterCode(code);
        chapter.setTitle(title);
        return chapter;
    }

    private Question buildQuestion(Long id, String code, Long chapterId, String rightAnswer) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode(code);
        question.setChapterId(chapterId);
        question.setRightAnswer(rightAnswer);
        question.setState(1);
        return question;
    }
}
