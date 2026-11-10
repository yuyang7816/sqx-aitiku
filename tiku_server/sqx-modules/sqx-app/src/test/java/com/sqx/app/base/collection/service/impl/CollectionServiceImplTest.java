package com.sqx.app.base.collection.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.collection.domain.bo.AppCollectionCreateBO;
import com.sqx.app.base.collection.domain.bo.AppCollectionQueryBO;
import com.sqx.app.base.collection.domain.vo.AppCollectionItemVO;
import com.sqx.app.base.collection.domain.vo.AppCollectionToggleResultVO;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.entity.UserQuestionCollection;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import com.sqx.system.db.service.IUserQuestionCollectionService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET/POST/DELETE /app/collection 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CollectionServiceImplTest {

    @Mock
    private IUserQuestionCollectionService iUserQuestionCollectionService;

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @InjectMocks
    private CollectionServiceImpl collectionService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserQuestionCollection.class);
        TableInfoHelper.initTableInfo(assistant, Question.class);
        TableInfoHelper.initTableInfo(assistant, UserQuestionAnswer.class);
    }

    @Test
    void listCollections_shouldReturnItemsWithSubjectAndQuestionDetail() {
        UserQuestionCollection collection = buildCollection(1L, 100L, 101L, "数学 · 初三");
        Question question = buildQuestion(101L, "rational-numbers-q01", "下列各数中，属于负数的是（ ）");
        UserQuestionAnswer answer = buildAnswer(100L, 101L, "A");

        when(iUserQuestionCollectionService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(collection));
        when(iQuestionService.listByIds(any())).thenReturn(Collections.singletonList(question));
        when(iUserQuestionAnswerService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(answer));

        List<AppCollectionItemVO> result = collectionService.listCollections(100L, new AppCollectionQueryBO());

        assertEquals(1, result.size());
        AppCollectionItemVO item = result.get(0);
        assertEquals("rational-numbers-q01", item.getId());
        assertEquals("数学 · 初三", item.getSubject());
        assertEquals("下列各数中，属于负数的是（ ）", item.getTitle());
        assertEquals("A", item.getMyAnswer());
        assertEquals("C", item.getRightAnswer());
        assertNotNull(item.getOptions());
        assertEquals(1, item.getOptions().size());
    }

    @Test
    void listCollections_whenFilterSubject_shouldApplyWrapper() {
        when(iUserQuestionCollectionService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppCollectionQueryBO query = new AppCollectionQueryBO();
        query.setSubject("数学 · 初三");
        List<AppCollectionItemVO> result = collectionService.listCollections(100L, query);

        assertTrue(result.isEmpty());
        verify(iUserQuestionCollectionService).list(any(Wrapper.class));
    }

    @Test
    void addCollection_whenNew_shouldSaveAndReturnCollected() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);
        when(iUserQuestionCollectionService.getOne(any(Wrapper.class))).thenReturn(null);
        when(iUserQuestionAnswerService.getOne(any(Wrapper.class))).thenReturn(null);

        AppCollectionCreateBO create = new AppCollectionCreateBO();
        create.setQuestionId("rational-numbers-q01");
        create.setSubject("数学 · 初三");

        AppCollectionToggleResultVO result = collectionService.addCollection(100L, create);

        assertTrue(result.getCollected());
        assertEquals("数学 · 初三", result.getItem().getSubject());
        ArgumentCaptor<UserQuestionCollection> captor = ArgumentCaptor.forClass(UserQuestionCollection.class);
        verify(iUserQuestionCollectionService).save(captor.capture());
        assertEquals(100L, captor.getValue().getUserId());
        assertEquals(101L, captor.getValue().getQuestionId());
    }

    @Test
    void addCollection_whenDuplicate_shouldUpdateSubjectOnly() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        UserQuestionCollection existing = buildCollection(10L, 100L, 101L, "旧学科");

        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);
        when(iUserQuestionCollectionService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(iUserQuestionAnswerService.getOne(any(Wrapper.class))).thenReturn(null);

        AppCollectionCreateBO create = new AppCollectionCreateBO();
        create.setQuestionId("rational-numbers-q01");
        create.setSubject("数学 · 初三");

        AppCollectionToggleResultVO result = collectionService.addCollection(100L, create);

        assertTrue(result.getCollected());
        verify(iUserQuestionCollectionService, never()).save(any());
        verify(iUserQuestionCollectionService).updateById(existing);
        assertEquals("数学 · 初三", existing.getSubject());
    }

    @Test
    void addCollection_whenQuestionNotFound_shouldThrow() {
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(null);

        AppCollectionCreateBO create = new AppCollectionCreateBO();
        create.setQuestionId("unknown-q");
        create.setSubject("数学 · 初三");

        assertThrows(BizException.class, () -> collectionService.addCollection(100L, create));
    }

    @Test
    void removeCollection_shouldDeleteByUserAndQuestion() {
        Question question = buildQuestion(101L, "rational-numbers-q01", "题干");
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);

        collectionService.removeCollection(100L, "rational-numbers-q01");

        verify(iUserQuestionCollectionService).remove(any(Wrapper.class));
    }

    @Test
    void removeCollection_whenQuestionNotFound_shouldNoOp() {
        when(iQuestionService.getById(999L)).thenReturn(null);

        collectionService.removeCollection(100L, "999");

        verify(iUserQuestionCollectionService, never()).remove(any(Wrapper.class));
    }

    private UserQuestionCollection buildCollection(Long id, Long userId, Long questionId, String subject) {
        UserQuestionCollection collection = new UserQuestionCollection();
        collection.setId(id);
        collection.setUserId(userId);
        collection.setQuestionId(questionId);
        collection.setSubject(subject);
        collection.setCreateTime(LocalDateTime.of(2026, 6, 12, 10, 0));
        return collection;
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

    private UserQuestionAnswer buildAnswer(Long userId, Long questionId, String userAnswer) {
        UserQuestionAnswer answer = new UserQuestionAnswer();
        answer.setUserId(userId);
        answer.setQuestionId(questionId);
        answer.setUserAnswer(userAnswer);
        return answer;
    }
}
