package com.sqx.app.base.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.notebook.domain.bo.AppNotebookCreateBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookQueryBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookUpdateBO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookAddResultVO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.notebook.support.NotebookItemAssembler;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IQuestionService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET/POST/DELETE /app/notebook 单元测试
 */
@ExtendWith(MockitoExtension.class)
class NotebookServiceImplTest {

    @Mock
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Mock
    private IQuestionService iQuestionService;

    @InjectMocks
    private NotebookServiceImpl notebookService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserQuestionWrong.class);
        TableInfoHelper.initTableInfo(assistant, Question.class);
    }

    @Test
    void listNotebook_shouldReturnItemsWithSubjectAndQuestionDetail() {
        UserQuestionWrong wrong = buildWrong(1L, 100L, 101L, "数学 · 初三", 2, 0);
        Question question = buildQuestion(101L, "rational-numbers-q01", "下列各数中，属于负数的是？");

        when(iUserQuestionWrongService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(wrong));
        when(iQuestionService.listByIds(any())).thenReturn(Collections.singletonList(question));

        List<AppNotebookItemVO> result = notebookService.listNotebook(100L, new AppNotebookQueryBO());

        assertEquals(1, result.size());
        AppNotebookItemVO item = result.get(0);
        assertEquals("rational-numbers-q01", item.getId());
        assertEquals("数学 · 初三", item.getSubject());
        assertEquals("下列各数中，属于负数的是？", item.getTitle());
        assertEquals("下列各数中，属于负数的是？", item.getQuestion());
        assertEquals("A", item.getWrong());
        assertEquals("C", item.getRight());
        assertEquals(2, item.getWrongCount());
        assertFalse(item.getMastered());
        assertNotNull(item.getOptions());
    }

    @Test
    void listNotebook_whenFilterSubjectAndMastered_shouldApplyWrapper() {
        when(iUserQuestionWrongService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppNotebookQueryBO query = new AppNotebookQueryBO();
        query.setSubject("数学 · 初三");
        query.setMastered(Boolean.FALSE);
        List<AppNotebookItemVO> result = notebookService.listNotebook(100L, query);

        assertTrue(result.isEmpty());
        verify(iUserQuestionWrongService).list(any(Wrapper.class));
    }

    @Test
    void addNotebook_whenNew_shouldSaveAndReturnAdded() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);
        when(iUserQuestionWrongService.getOne(any(Wrapper.class))).thenReturn(null);

        AppNotebookCreateBO create = new AppNotebookCreateBO();
        create.setQuestionId("rational-numbers-q01");
        create.setSubject("数学 · 初三");
        create.setUserAnswer("A");

        AppNotebookAddResultVO result = notebookService.addNotebook(100L, create);

        assertTrue(result.getAdded());
        assertFalse(result.getDuplicate());
        assertEquals("数学 · 初三", result.getItem().getSubject());
        ArgumentCaptor<UserQuestionWrong> captor = ArgumentCaptor.forClass(UserQuestionWrong.class);
        verify(iUserQuestionWrongService).save(captor.capture());
        assertEquals(100L, captor.getValue().getUserId());
        assertEquals(101L, captor.getValue().getQuestionId());
        assertEquals(1, captor.getValue().getWrongCount());
    }

    @Test
    void addNotebook_whenDuplicate_shouldIncrementWrongCountAndResetMastered() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        UserQuestionWrong existing = buildWrong(10L, 100L, 101L, "旧学科", 3, 0);

        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);
        when(iUserQuestionWrongService.getOne(any(Wrapper.class))).thenReturn(existing);

        AppNotebookCreateBO create = new AppNotebookCreateBO();
        create.setQuestionId("rational-numbers-q01");
        create.setSubject("数学 · 初三");
        create.setUserAnswer("B");

        AppNotebookAddResultVO result = notebookService.addNotebook(100L, create);

        assertFalse(result.getAdded());
        assertTrue(result.getDuplicate());
        verify(iUserQuestionWrongService, never()).save(any());
        verify(iUserQuestionWrongService).updateById(existing);
        assertEquals("数学 · 初三", existing.getSubject());
        assertEquals("B", existing.getUserAnswer());
        assertEquals(4, existing.getWrongCount());
        assertEquals(UserQuestionWrong.MASTERED_NO, existing.getMastered());
    }

    @Test
    void addNotebook_whenQuestionNotFound_shouldThrow() {
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(null);

        AppNotebookCreateBO create = new AppNotebookCreateBO();
        create.setQuestionId("unknown-q");
        create.setSubject("数学 · 初三");

        assertThrows(BizException.class, () -> notebookService.addNotebook(100L, create));
    }

    @Test
    void removeNotebook_shouldDeleteByUserAndQuestion() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);

        notebookService.removeNotebook(100L, "rational-numbers-q01");

        verify(iUserQuestionWrongService).remove(any(Wrapper.class));
    }

    @Test
    void removeNotebook_whenQuestionNotFound_shouldNoOp() {
        when(iQuestionService.getById(999L)).thenReturn(null);

        notebookService.removeNotebook(100L, "999");

        verify(iUserQuestionWrongService, never()).remove(any(Wrapper.class));
    }

    @Test
    void updateMastered_shouldToggleAndReturnItem() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        UserQuestionWrong existing = buildWrong(10L, 100L, 101L, "数学 · 初三", 2, 0);

        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);
        when(iUserQuestionWrongService.getOne(any(Wrapper.class))).thenReturn(existing);

        AppNotebookUpdateBO update = new AppNotebookUpdateBO();
        update.setQuestionId("rational-numbers-q01");
        update.setMastered(Boolean.TRUE);

        AppNotebookItemVO result = notebookService.updateMastered(100L, update);

        assertTrue(result.getMastered());
        verify(iUserQuestionWrongService).updateById(existing);
        assertEquals(UserQuestionWrong.MASTERED_YES, existing.getMastered());
    }

    @Test
    void clearNotebook_shouldRemoveAllForUser() {
        notebookService.clearNotebook(100L);
        verify(iUserQuestionWrongService).remove(any(Wrapper.class));
    }

    private UserQuestionWrong buildWrong(Long id, Long userId, Long questionId, String subject,
                                         int wrongCount, int mastered) {
        UserQuestionWrong wrong = new UserQuestionWrong();
        wrong.setId(id);
        wrong.setUserId(userId);
        wrong.setQuestionId(questionId);
        wrong.setSubject(subject);
        wrong.setUserAnswer("A");
        wrong.setWrongCount(wrongCount);
        wrong.setMastered(mastered);
        wrong.setLastPracticeAt(LocalDateTime.of(2026, 6, 12, 10, 0));
        wrong.setCreateTime(LocalDateTime.of(2026, 6, 12, 9, 0));
        return wrong;
    }

    private Question buildQuestion(Long id, String code, String title) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode(code);
        question.setTitle(title);
        question.setRightAnswer("C");
        question.setAnalysis("解析内容");
        question.setOptionsJson("[{\"label\":\"A\",\"value\":\"-3\"}]");
        question.setTagsJson("[\"#负数定义\"]");
        question.setState(1);
        return question;
    }
}
