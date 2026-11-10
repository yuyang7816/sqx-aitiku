package com.sqx.app.base.question.paper.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.question.paper.constants.QuestionPaperConstant;
import com.sqx.app.base.question.paper.domain.bo.AppQuestionPaperListQueryBO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperItemVO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperQuestionVO;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionPaper;
import com.sqx.system.db.entity.QuestionPaperItem;
import com.sqx.system.db.service.IQuestionPaperItemService;
import com.sqx.system.db.service.IQuestionPaperService;
import com.sqx.system.db.service.IQuestionService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/question/paper/list 与 GET /app/question/paper/{id}/questions 单元测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionPaperServiceImplTest {

    @Mock
    private IQuestionPaperService iQuestionPaperService;

    @Mock
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Mock
    private IQuestionService iQuestionService;

    @InjectMocks
    private QuestionPaperServiceImpl questionPaperService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, QuestionPaper.class);
        TableInfoHelper.initTableInfo(assistant, QuestionPaperItem.class);
        TableInfoHelper.initTableInfo(assistant, Question.class);
    }

    @Test
    void listPapers_shouldReturnEnabledPapersByType() {
        QuestionPaper special = buildPaper(1L, "special-rational-numbers", "有理数专项训练",
                QuestionPaperConstant.TYPE_SPECIAL, 2);
        QuestionPaper exam = buildPaper(2L, "exam-math-2025-mock", "2025 中考数学模拟卷",
                QuestionPaperConstant.TYPE_EXAM, 3);

        when(iQuestionPaperService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(special));

        AppQuestionPaperListQueryBO query = new AppQuestionPaperListQueryBO();
        query.setType(QuestionPaperConstant.TYPE_SPECIAL);
        List<AppQuestionPaperItemVO> result = questionPaperService.listPapers(query);

        assertEquals(1, result.size());
        AppQuestionPaperItemVO first = result.get(0);
        assertEquals("special-rational-numbers", first.getPaperId());
        assertEquals("有理数专项训练", first.getTitle());
        assertEquals(QuestionPaperConstant.TYPE_SPECIAL, first.getType());
        assertEquals(2, first.getQuestionCount());

        verify(iQuestionPaperService).list(any(Wrapper.class));
    }

    @Test
    void listPapers_whenNoMatch_shouldReturnEmptyList() {
        when(iQuestionPaperService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppQuestionPaperListQueryBO query = new AppQuestionPaperListQueryBO();
        query.setType(QuestionPaperConstant.TYPE_EXAM);
        List<AppQuestionPaperItemVO> result = questionPaperService.listPapers(query);

        assertTrue(result.isEmpty());
    }

    @Test
    void listPaperQuestions_whenPaperNotFound_shouldThrowBizException() {
        when(iQuestionPaperService.getOne(any(Wrapper.class))).thenReturn(null);

        assertThrows(BizException.class,
                () -> questionPaperService.listPaperQuestions("unknown-paper"));
    }

    @Test
    void listPaperQuestions_shouldReturnOrderedQuestionsWithOptions() {
        QuestionPaper paper = buildPaper(1L, "exam-math-2025-mock", "2025 中考数学模拟卷",
                QuestionPaperConstant.TYPE_EXAM, 2);
        QuestionPaperItem item1 = buildPaperItem(11L, 1L, 101L, 1);
        QuestionPaperItem item2 = buildPaperItem(12L, 1L, 102L, 2);
        Question q1 = buildQuestion(101L, "rational-numbers-q01", "下列各数中，属于负数的是（ ）", 1,
                "A", "[{\"label\":\"A\",\"value\":\"-3\"},{\"label\":\"B\",\"value\":\"0\"}]");
        Question q2 = buildQuestion(102L, "rational-numbers-q02", "计算 (-2) + (-3) 的结果是（ ）", 2,
                "B", "[{\"label\":\"A\",\"value\":\"-1\"},{\"label\":\"B\",\"value\":\"-5\"}]");

        when(iQuestionPaperService.getOne(any(Wrapper.class))).thenReturn(paper);
        when(iQuestionPaperItemService.list(any(Wrapper.class))).thenReturn(Arrays.asList(item1, item2));
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(q1, q2));

        List<AppQuestionPaperQuestionVO> result = questionPaperService.listPaperQuestions("exam-math-2025-mock");

        assertEquals(2, result.size());
        AppQuestionPaperQuestionVO first = result.get(0);
        assertEquals("01", first.getNo());
        assertEquals(101L, first.getQuestionId());
        assertEquals("rational-numbers-q01", first.getQuestionCode());
        assertEquals("下列各数中，属于负数的是（ ）", first.getTitle());
        assertEquals("A", first.getRightAnswer());
        assertEquals(2, first.getOptions().size());
        assertEquals("A", first.getOptions().get(0).getLabel());
        assertEquals("-3", first.getOptions().get(0).getValue());

        AppQuestionPaperQuestionVO second = result.get(1);
        assertEquals("02", second.getNo());
        assertEquals(102L, second.getQuestionId());
    }

    @Test
    void listPaperQuestions_whenNoItems_shouldReturnEmptyList() {
        QuestionPaper paper = buildPaper(1L, "special-rational-numbers", "有理数专项训练",
                QuestionPaperConstant.TYPE_SPECIAL, 0);

        when(iQuestionPaperService.getOne(any(Wrapper.class))).thenReturn(paper);
        when(iQuestionPaperItemService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        List<AppQuestionPaperQuestionVO> result = questionPaperService.listPaperQuestions("special-rational-numbers");

        assertTrue(result.isEmpty());
        verify(iQuestionService, never()).list(any(Wrapper.class));
    }

    @Test
    void formatNo_shouldZeroPadSortOrder() {
        assertEquals("01", questionPaperService.formatNo(1));
        assertEquals("12", questionPaperService.formatNo(12));
    }

    private QuestionPaper buildPaper(Long id, String code, String title, String type, int questionCount) {
        QuestionPaper paper = new QuestionPaper();
        paper.setId(id);
        paper.setPaperCode(code);
        paper.setTitle(title);
        paper.setPaperType(type);
        paper.setDescription("简介");
        paper.setGrade("初中三年级");
        paper.setGradeShort("初三");
        paper.setSubject("数学");
        paper.setQuestionCount(questionCount);
        paper.setSortOrder(1);
        paper.setState(QuestionPaperConstant.STATE_ENABLED);
        return paper;
    }

    private QuestionPaperItem buildPaperItem(Long id, Long paperId, Long questionId, int sortOrder) {
        QuestionPaperItem item = new QuestionPaperItem();
        item.setId(id);
        item.setPaperId(paperId);
        item.setQuestionId(questionId);
        item.setSortOrder(sortOrder);
        return item;
    }

    private Question buildQuestion(Long id, String code, String title, int sortOrder,
                                   String rightAnswer, String optionsJson) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode(code);
        question.setTitle(title);
        question.setQuestionType("单选题");
        question.setDifficulty("简单");
        question.setRightAnswer(rightAnswer);
        question.setAnalysis("解析内容");
        question.setOptionsJson(optionsJson);
        question.setSortOrder(sortOrder);
        question.setState(1);
        return question;
    }
}
