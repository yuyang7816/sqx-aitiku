package com.sqx.app.base.question.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.question.manage.constants.QuestionPracticeEntry;
import com.sqx.app.base.question.manage.domain.bo.QuestionInfoBo;
import com.sqx.app.base.question.manage.domain.bo.QuestionOptionBo;
import com.sqx.app.base.question.manage.domain.bo.QuestionSaveBo;
import com.sqx.app.base.question.manage.domain.vo.QuestionVo;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.QuestionSection;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IKnowledgePointQuestionService;
import com.sqx.system.db.service.IKnowledgePointService;
import com.sqx.system.db.service.IQuestionPaperItemService;
import com.sqx.system.db.service.IQuestionPaperService;
import com.sqx.system.db.service.IQuestionSectionService;
import com.sqx.system.db.service.IQuestionService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理端题目 CRUD 单元测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionAdminServiceImplTest {

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private IQuestionChapterService iQuestionChapterService;

    @Mock
    private IQuestionSectionService iQuestionSectionService;

    @Mock
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Mock
    private IQuestionPaperService iQuestionPaperService;

    @Mock
    private IKnowledgePointService iKnowledgePointService;

    @Mock
    private IKnowledgePointQuestionService iKnowledgePointQuestionService;

    @InjectMocks
    private QuestionAdminServiceImpl questionAdminService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, Question.class);
        TableInfoHelper.initTableInfo(assistant, QuestionChapter.class);
        TableInfoHelper.initTableInfo(assistant, QuestionSection.class);
    }

    @Test
    void saveInfo_shouldPersistQuestionWithChapterAndOptions() {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(10L);
        chapter.setChapterCode("rational-numbers");
        when(iQuestionChapterService.getOne(any(QueryWrapper.class), any(Boolean.class))).thenReturn(chapter);
        when(iQuestionService.count(any(QueryWrapper.class))).thenReturn(0L);
        when(iQuestionService.save(any(Question.class))).thenAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            q.setId(100L);
            return true;
        });
        when(iQuestionPaperItemService.list()).thenReturn(Collections.emptyList());
        when(iKnowledgePointService.list()).thenReturn(Collections.emptyList());
        when(iQuestionPaperService.list()).thenReturn(Collections.emptyList());

        QuestionSaveBo saveBo = new QuestionSaveBo();
        saveBo.setPracticeEntry(QuestionPracticeEntry.CHAPTER);
        saveBo.setSubject("数学");
        saveBo.setChapterCode("rational-numbers");
        saveBo.setTitle("测试题干");
        saveBo.setQuestionType("单选题");
        saveBo.setDifficulty("简单");
        saveBo.setRightAnswer("A");
        saveBo.setAnalysis("负数是小于0的数，-3符合题意。");
        saveBo.setVideoUrl("http://127.0.0.1:9135/sqx_fast/upload/2026/08/26/demo-q01.mp4");
        saveBo.setSortOrder(1);
        saveBo.setState(1);
        QuestionOptionBo option = new QuestionOptionBo();
        option.setLabel("A");
        option.setValue("-3");
        saveBo.setOptions(Collections.singletonList(option));
        saveBo.setTags(Arrays.asList("有理数", "基础"));

        questionAdminService.saveInfo(saveBo);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(iQuestionService).save(captor.capture());
        Question saved = captor.getValue();
        assertNotNull(saved.getQuestionCode());
        assertTrue(saved.getQuestionCode().startsWith("q-"));
        assertEquals(10L, saved.getChapterId());
        assertEquals("[{\"label\":\"A\",\"value\":\"-3\"}]", saved.getOptionsJson());
        assertEquals("[\"有理数\",\"基础\"]", saved.getTagsJson());
        assertEquals("/upload/2026/08/26/demo-q01.mp4", saved.getVideoUrl());
    }

    @Test
    void saveInfo_shouldRejectDuplicateQuestionCode() {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(10L);
        chapter.setChapterCode("rational-numbers");
        when(iQuestionChapterService.getOne(any(QueryWrapper.class), any(Boolean.class))).thenReturn(chapter);
        when(iQuestionService.count(any(QueryWrapper.class))).thenReturn(1L);

        QuestionSaveBo saveBo = new QuestionSaveBo();
        saveBo.setQuestionCode("dup-q01");
        saveBo.setPracticeEntry(QuestionPracticeEntry.CHAPTER);
        saveBo.setSubject("数学");
        saveBo.setChapterCode("rational-numbers");
        saveBo.setTitle("重复编码");
        saveBo.setQuestionType("单选题");
        saveBo.setAnalysis("测试解析");
        saveBo.setVideoUrl("/upload/2026/08/26/demo.mp4");
        saveBo.setSortOrder(1);
        saveBo.setState(1);

        assertThrows(BizException.class, () -> questionAdminService.saveInfo(saveBo));
    }

    @Test
    void saveInfo_shouldRejectUnauthorizedExternalVideoUrl() {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(10L);
        chapter.setChapterCode("rational-numbers");
        when(iQuestionChapterService.getOne(any(QueryWrapper.class), any(Boolean.class))).thenReturn(chapter);
        when(iQuestionService.count(any(QueryWrapper.class))).thenReturn(0L);

        QuestionSaveBo saveBo = new QuestionSaveBo();
        saveBo.setPracticeEntry(QuestionPracticeEntry.CHAPTER);
        saveBo.setSubject("数学");
        saveBo.setChapterCode("rational-numbers");
        saveBo.setTitle("外部视频");
        saveBo.setQuestionType("单选题");
        saveBo.setAnalysis("测试解析");
        saveBo.setVideoUrl("https://example.com/demo.mp4");
        saveBo.setSortOrder(1);
        saveBo.setState(1);

        assertThrows(BizException.class, () -> questionAdminService.saveInfo(saveBo));
    }

    @Test
    void info_shouldReturnParsedOptionsAndTags() {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(10L);
        chapter.setChapterCode("rational-numbers");
        chapter.setTitle("有理数");
        chapter.setGrade("七年级");
        chapter.setSubject("数学");
        chapter.setVersion("人教版");

        Question question = new Question();
        question.setId(1L);
        question.setQuestionCode("rational-numbers-q01");
        question.setChapterId(10L);
        question.setTitle("题干");
        question.setQuestionType("单选题");
        question.setDifficulty("简单");
        question.setRightAnswer("A");
        question.setOptionsJson("[{\"label\":\"A\",\"value\":\"-3\"}]");
        question.setTagsJson("[\"有理数\"]");
        question.setSortOrder(1);
        question.setState(1);

        when(iQuestionService.getById(1L)).thenReturn(question);
        when(iQuestionPaperItemService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(iKnowledgePointQuestionService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(iQuestionChapterService.listByIds(Collections.singleton(10L))).thenReturn(Collections.singletonList(chapter));

        QuestionInfoBo infoBo = new QuestionInfoBo();
        infoBo.setId(1L);
        QuestionVo result = questionAdminService.info(infoBo);

        assertNotNull(result);
        assertEquals("rational-numbers-q01", result.getQuestionCode());
        assertEquals("有理数", result.getChapterTitle());
        assertEquals(1, result.getOptions().size());
        assertEquals("A", result.getOptions().get(0).getLabel());
        assertEquals(1, result.getTags().size());
        assertEquals("有理数", result.getTags().get(0));
    }
}
