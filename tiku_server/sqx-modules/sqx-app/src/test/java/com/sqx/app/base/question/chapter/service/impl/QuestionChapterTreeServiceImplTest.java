package com.sqx.app.base.question.chapter.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.chapter.domain.bo.AppQuestionChapterTreeQueryBO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionChapterNodeVO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionSectionNodeVO;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.QuestionSection;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionSectionService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionChapterTreeServiceImplTest {

    @Mock
    private IQuestionChapterService iQuestionChapterService;

    @Mock
    private IQuestionSectionService iQuestionSectionService;

    @InjectMocks
    private QuestionChapterTreeServiceImpl questionChapterTreeService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, QuestionChapter.class);
        TableInfoHelper.initTableInfo(assistant, QuestionSection.class);
    }

    @Test
    void getChapterTree_whenNoChapters_shouldReturnEmptyList() {
        when(iQuestionChapterService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        AppQuestionChapterTreeQueryBO query = buildQuery("初一年级", "数学", "人教版");
        List<AppQuestionChapterNodeVO> result = questionChapterTreeService.getChapterTree(query);

        assertTrue(result.isEmpty());
        verify(iQuestionChapterService).list(any(Wrapper.class));
        verify(iQuestionSectionService, never()).list(any(Wrapper.class));
    }

    @Test
    void getChapterTree_shouldMapChaptersWithSectionsBatch() {
        QuestionChapter chapter = buildChapter(1L, "rational-numbers", "第一章 有理数", "初一", 45, 1);
        QuestionSection section1 = buildSection(11L, 1L, "rational-numbers-1-1", "1.1 正数和负数", 12, 1);
        QuestionSection section2 = buildSection(12L, 1L, "rational-numbers-1-2", "1.2 有理数", 15, 2);

        when(iQuestionChapterService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(chapter));
        when(iQuestionSectionService.list(any(Wrapper.class))).thenReturn(Arrays.asList(section1, section2));

        List<AppQuestionChapterNodeVO> result = questionChapterTreeService.getChapterTree(
                buildQuery("初一年级", "数学", "人教版"));

        assertEquals(1, result.size());
        AppQuestionChapterNodeVO chapterNode = result.get(0);
        assertEquals("rational-numbers", chapterNode.getCode());
        assertEquals("第一章 有理数", chapterNode.getTitle());
        assertEquals(1, chapterNode.getSort());
        assertEquals(45, chapterNode.getQuestionCount());
        assertEquals("初一", chapterNode.getGradeShort());

        List<AppQuestionSectionNodeVO> sections = chapterNode.getSections();
        assertEquals(2, sections.size());
        assertEquals("rational-numbers-1-1", sections.get(0).getCode());
        assertEquals("1.1 正数和负数", sections.get(0).getTitle());
        assertEquals(12, sections.get(0).getQuestionCount());
        assertEquals("rational-numbers-1-2", sections.get(1).getCode());

        verify(iQuestionChapterService).list(any(Wrapper.class));
        verify(iQuestionSectionService, times(1)).list(any(Wrapper.class));
    }

    @Test
    void getChapterTree_shouldNormalizeVersionBeforeQuery() {
        when(iQuestionChapterService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        questionChapterTreeService.getChapterTree(buildQuery("初一年级", "数学", "  人教版（默认）  "));

        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(iQuestionChapterService).list(captor.capture());
        @SuppressWarnings("unchecked")
        QueryWrapper<QuestionChapter> wrapper = (QueryWrapper<QuestionChapter>) captor.getValue();
        String sqlSegment = wrapper.getSqlSegment();
        assertTrue(sqlSegment.contains("version"));
        assertTrue(wrapper.getParamNameValuePairs().containsValue("人教版"));
    }

    @Test
    void getChapterTree_shouldFilterByGradeSubjectVersionAndEnabledState() {
        when(iQuestionChapterService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        questionChapterTreeService.getChapterTree(buildQuery("初一年级", "数学", "人教版"));

        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(iQuestionChapterService).list(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("grade"));
        assertTrue(sqlSegment.contains("subject"));
        assertTrue(sqlSegment.contains("version"));
        assertTrue(sqlSegment.contains("state"));
        assertTrue(sqlSegment.contains(String.valueOf(QuestionChapterConstant.STATE_ENABLED)));
    }

    private AppQuestionChapterTreeQueryBO buildQuery(String grade, String subject, String version) {
        AppQuestionChapterTreeQueryBO query = new AppQuestionChapterTreeQueryBO();
        query.setGrade(grade);
        query.setSubject(subject);
        query.setVersion(version);
        return query;
    }

    private QuestionChapter buildChapter(Long id, String code, String title, String gradeShort, Integer questionCount, Integer sort) {
        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(id);
        chapter.setChapterCode(code);
        chapter.setTitle(title);
        chapter.setGradeShort(gradeShort);
        chapter.setQuestionCount(questionCount);
        chapter.setSortOrder(sort);
        return chapter;
    }

    private QuestionSection buildSection(Long id, Long chapterId, String code, String title, Integer questionCount, Integer sort) {
        QuestionSection section = new QuestionSection();
        section.setId(id);
        section.setChapterId(chapterId);
        section.setSectionCode(code);
        section.setTitle(title);
        section.setQuestionCount(questionCount);
        section.setSortOrder(sort);
        return section;
    }
}
