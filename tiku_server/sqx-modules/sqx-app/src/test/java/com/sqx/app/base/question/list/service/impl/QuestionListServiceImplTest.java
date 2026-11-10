package com.sqx.app.base.question.list.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.question.list.constants.QuestionAnswerStatus;
import com.sqx.app.base.question.list.constants.QuestionListMode;
import com.sqx.app.base.question.list.domain.bo.AppQuestionListQueryBO;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.KnowledgePoint;
import com.sqx.system.db.entity.KnowledgePointQuestion;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.QuestionPaper;
import com.sqx.system.db.entity.QuestionPaperItem;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.service.IKnowledgePointQuestionService;
import com.sqx.system.db.service.IKnowledgePointService;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionPaperItemService;
import com.sqx.system.db.service.IQuestionPaperService;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/question/list 练习题目拉取单元测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionListServiceImplTest {

    @Mock
    private IQuestionChapterService iQuestionChapterService;

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @Mock
    private IQuestionPaperService iQuestionPaperService;

    @Mock
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Mock
    private IKnowledgePointService iKnowledgePointService;

    @Mock
    private IKnowledgePointQuestionService iKnowledgePointQuestionService;

    @InjectMocks
    private QuestionListServiceImpl questionListService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, QuestionChapter.class);
        TableInfoHelper.initTableInfo(assistant, Question.class);
        TableInfoHelper.initTableInfo(assistant, UserQuestionAnswer.class);
        TableInfoHelper.initTableInfo(assistant, QuestionPaper.class);
        TableInfoHelper.initTableInfo(assistant, QuestionPaperItem.class);
    }

    @Test
    void validateQuery_whenNoSource_shouldThrow() {
        AppQuestionListQueryBO query = new AppQuestionListQueryBO();
        BizException ex = assertThrows(BizException.class, () -> questionListService.validateQuery(query));
        assertTrue(ex.getMessage().contains("至少传一项"));
    }

    @Test
    void validateQuery_whenInvalidMode_shouldThrow() {
        AppQuestionListQueryBO query = buildQuery("rational-numbers", 1L, 10L);
        query.setMode("invalid");
        BizException ex = assertThrows(BizException.class, () -> questionListService.validateQuery(query));
        assertTrue(ex.getMessage().contains("mode"));
    }

    @Test
    void validateQuery_whenSingleWithoutQuestionIds_shouldThrow() {
        AppQuestionListQueryBO query = buildQuery("rational-numbers", 1L, 10L);
        query.setMode(QuestionListMode.SINGLE);
        BizException ex = assertThrows(BizException.class, () -> questionListService.validateQuery(query));
        assertTrue(ex.getMessage().contains("questionIds"));
    }

    @Test
    void pageQuestionList_whenChapterNotFound_shouldReturnEmptyPage() {
        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(null);

        AppQuestionListQueryBO query = buildQuery("unknown-chapter", 1L, 10L);
        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(100L, query);

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(iQuestionService, never()).page(any(Page.class), any(Wrapper.class));
    }

    @Test
    void pageQuestionList_shouldReturnPaginatedQuestionsWithAnswerStatusAndDetails() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers");
        Question q1 = buildFullQuestion(101L, "rational-numbers-q01", 1L, "下列各数中，属于负数的是（ ）", 1);
        Question q2 = buildFullQuestion(102L, "rational-numbers-q02", 1L, "计算 (-2) + (-3) 的结果是（ ）", 2);
        Question q3 = buildFullQuestion(103L, "rational-numbers-q03", 1L, "一个数的绝对值是5，则这个数是（ ）", 3);

        Page<Question> entityPage = new Page<>(1, 50);
        entityPage.setTotal(3);
        entityPage.setRecords(Arrays.asList(q1, q2, q3));

        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.page(any(Page.class), any(Wrapper.class))).thenReturn(entityPage);
        when(iUserQuestionAnswerService.list(any(Wrapper.class))).thenReturn(Arrays.asList(
                buildAnswer(100L, 101L, QuestionAnswerStatus.CORRECT_YES),
                buildAnswer(100L, 102L, QuestionAnswerStatus.CORRECT_NO)
        ));

        AppQuestionListQueryBO query = buildQuery("rational-numbers", 1L, 50L);
        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(100L, query);

        assertEquals(3, result.getTotal());
        assertEquals(3, result.getRecords().size());

        AppQuestionListItemVO first = result.getRecords().get(0);
        assertEquals("01", first.getNo());
        assertEquals(101L, first.getQuestionId());
        assertEquals("下列各数中，属于负数的是（ ）", first.getText());
        assertEquals("下列各数中，属于负数的是（ ）", first.getTitle());
        assertEquals("单选题", first.getQuestionType());
        assertEquals("简单", first.getDifficulty());
        assertEquals("A", first.getRightAnswer());
        assertFalse(first.getAnalysis().isEmpty());
        assertEquals(2, first.getOptions().size());
        assertEquals(2, first.getTags().size());
        assertEquals(QuestionAnswerStatus.TYPE_RIGHT, first.getType());
        assertEquals(QuestionAnswerStatus.LABEL_RIGHT, first.getStatus());

        AppQuestionListItemVO second = result.getRecords().get(1);
        assertEquals(QuestionAnswerStatus.TYPE_WRONG, second.getType());

        AppQuestionListItemVO third = result.getRecords().get(2);
        assertEquals(QuestionAnswerStatus.TYPE_TODO, third.getType());
    }

    @Test
    void pageQuestionList_byKnowledgePoint_shouldReturnLinkedQuestions() {
        KnowledgePoint point = buildKnowledgePoint(20L, "seed-junior3-physics-kp");
        Question q1 = buildFullQuestion(201L, "physics-q01", 2L, "物理单选题", 1);
        Question q2 = buildFullQuestion(202L, "physics-q02", 2L, "物理多选题", 2);

        when(iKnowledgePointService.getOne(any(Wrapper.class))).thenReturn(point);
        when(iKnowledgePointQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(
                buildKnowledgeLink(20L, 201L, 1),
                buildKnowledgeLink(20L, 202L, 2)
        ));
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(q1, q2));
        when(iUserQuestionAnswerService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppQuestionListQueryBO query = new AppQuestionListQueryBO();
        query.setKnowledgePoint("seed-junior3-physics-kp");
        query.setPage(1L);
        query.setSize(10L);

        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(100L, query);

        assertEquals(2, result.getTotal());
        assertEquals("physics-q01", result.getRecords().get(0).getQuestionCode());
        verify(iQuestionChapterService, never()).getOne(any(Wrapper.class));
    }

    @Test
    void pageQuestionList_byPaperId_shouldReturnPaperQuestions() {
        QuestionPaper paper = buildPaper(10L, "special-rational-numbers");
        Question q1 = buildFullQuestion(101L, "rational-numbers-q01", 1L, "下列各数中，属于负数的是（ ）", 1);
        Question q2 = buildFullQuestion(102L, "rational-numbers-q02", 1L, "计算 (-2) + (-3) 的结果是（ ）", 2);

        when(iQuestionPaperService.getOne(any(Wrapper.class))).thenReturn(paper);
        when(iQuestionPaperItemService.list(any(Wrapper.class))).thenReturn(Arrays.asList(
                buildPaperItem(10L, 101L, 1),
                buildPaperItem(10L, 102L, 2)
        ));
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(q1, q2));
        when(iUserQuestionAnswerService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppQuestionListQueryBO query = new AppQuestionListQueryBO();
        query.setPaperId("special-rational-numbers");
        query.setPage(1L);
        query.setSize(10L);

        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(100L, query);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("rational-numbers-q01", result.getRecords().get(0).getQuestionCode());
        assertNotNull(result.getRecords().get(0).getOptions());
        verify(iQuestionChapterService, never()).getOne(any(Wrapper.class));
    }

    @Test
    void pageQuestionList_byQuestionIds_shouldReturnMatchedQuestions() {
        Question q1 = buildFullQuestion(101L, "rational-numbers-q01", 1L, "下列各数中，属于负数的是（ ）", 1);

        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(q1);
        when(iUserQuestionAnswerService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppQuestionListQueryBO query = new AppQuestionListQueryBO();
        query.setQuestionIds("rational-numbers-q01");
        query.setMode(QuestionListMode.SINGLE);
        query.setPage(1L);
        query.setSize(10L);

        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(100L, query);

        assertEquals(1, result.getTotal());
        assertEquals("rational-numbers-q01", result.getRecords().get(0).getQuestionCode());
        assertEquals("A", result.getRecords().get(0).getRightAnswer());
    }

    @Test
    void pageQuestionList_modeWrong_shouldReturnOnlyWrongQuestions() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers");
        Question q1 = buildFullQuestion(101L, "rational-numbers-q01", 1L, "题1", 1);
        Question q2 = buildFullQuestion(102L, "rational-numbers-q02", 1L, "题2", 2);
        Question q3 = buildFullQuestion(103L, "rational-numbers-q03", 1L, "题3", 3);

        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(q1, q2, q3));
        when(iUserQuestionAnswerService.list(any(Wrapper.class))).thenReturn(Arrays.asList(
                buildAnswer(100L, 101L, QuestionAnswerStatus.CORRECT_YES),
                buildAnswer(100L, 102L, QuestionAnswerStatus.CORRECT_NO)
        ));

        AppQuestionListQueryBO query = buildQuery("rational-numbers", 1L, 10L);
        query.setMode(QuestionListMode.WRONG);

        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(100L, query);

        assertEquals(1, result.getTotal());
        assertEquals(102L, result.getRecords().get(0).getQuestionId());
        assertEquals(QuestionAnswerStatus.TYPE_WRONG, result.getRecords().get(0).getType());
    }

    @Test
    void pageQuestionList_whenNoQuestions_shouldReturnEmptyRecords() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers");
        Page<Question> entityPage = new Page<>(1, 10);
        entityPage.setTotal(0);
        entityPage.setRecords(Collections.emptyList());

        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(chapter);
        when(iQuestionService.page(any(Page.class), any(Wrapper.class))).thenReturn(entityPage);

        IPage<AppQuestionListItemVO> result = questionListService.pageQuestionList(
                100L, buildQuery("rational-numbers", 1L, 10L));

        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(iUserQuestionAnswerService, never()).list(any(Wrapper.class));
    }

    @Test
    void pageQuestionList_shouldResolveChapterByBusinessCode() {
        when(iQuestionChapterService.getOne(any(Wrapper.class))).thenReturn(null);

        questionListService.pageQuestionList(100L, buildQuery("  rational-numbers  ", 1L, 10L));

        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(iQuestionChapterService).getOne(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("chapter_code"));
    }

    @Test
    void formatNo_shouldZeroPadSortOrder() {
        assertEquals("01", questionListService.formatNo(1));
        assertEquals("12", questionListService.formatNo(12));
    }

    private AppQuestionListQueryBO buildQuery(String chapterId, Long page, Long size) {
        AppQuestionListQueryBO query = new AppQuestionListQueryBO();
        query.setChapterId(chapterId);
        query.setPage(page);
        query.setSize(size);
        return query;
    }

    private QuestionChapter buildChapter(Long id, String code) {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(id);
        chapter.setChapterCode(code);
        chapter.setTitle("第一章：有理数");
        return chapter;
    }

    private QuestionPaper buildPaper(Long id, String code) {
        QuestionPaper paper = new QuestionPaper();
        paper.setId(id);
        paper.setPaperCode(code);
        paper.setState(1);
        return paper;
    }

    private QuestionPaperItem buildPaperItem(Long paperId, Long questionId, Integer sortOrder) {
        QuestionPaperItem item = new QuestionPaperItem();
        item.setPaperId(paperId);
        item.setQuestionId(questionId);
        item.setSortOrder(sortOrder);
        return item;
    }

    private KnowledgePoint buildKnowledgePoint(Long id, String code) {
        KnowledgePoint point = new KnowledgePoint();
        point.setId(id);
        point.setPointCode(code);
        point.setTitle("综合练习");
        point.setState(1);
        return point;
    }

    private KnowledgePointQuestion buildKnowledgeLink(Long pointId, Long questionId, int sortOrder) {
        KnowledgePointQuestion link = new KnowledgePointQuestion();
        link.setPointId(pointId);
        link.setQuestionId(questionId);
        link.setSortOrder(sortOrder);
        return link;
    }

    private Question buildFullQuestion(Long id, String code, Long chapterId, String title, Integer sortOrder) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode(code);
        question.setChapterId(chapterId);
        question.setTitle(title);
        question.setQuestionType("单选题");
        question.setDifficulty("简单");
        question.setRightAnswer("A");
        question.setAnalysis("解析内容");
        question.setOptionsJson("[{\"label\":\"A\",\"value\":\"-3\"},{\"label\":\"B\",\"value\":\"0\"}]");
        question.setTagsJson("[\"#标签1\",\"#标签2\"]");
        question.setSortOrder(sortOrder);
        question.setState(1);
        return question;
    }

    private UserQuestionAnswer buildAnswer(Long userId, Long questionId, int isCorrect) {
        UserQuestionAnswer answer = new UserQuestionAnswer();
        answer.setUserId(userId);
        answer.setQuestionId(questionId);
        answer.setIsCorrect(isCorrect);
        return answer;
    }
}
