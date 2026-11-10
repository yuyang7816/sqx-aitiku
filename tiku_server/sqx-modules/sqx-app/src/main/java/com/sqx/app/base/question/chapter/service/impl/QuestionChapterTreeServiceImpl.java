package com.sqx.app.base.question.chapter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.chapter.domain.bo.AppQuestionChapterTreeQueryBO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionChapterNodeVO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionSectionNodeVO;
import com.sqx.app.base.question.chapter.service.QuestionChapterTreeService;
import com.sqx.app.base.setup.support.SetupQueryResolveSupport;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.QuestionSection;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionSectionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户端题库章节树服务实现
 */
@Service
public class QuestionChapterTreeServiceImpl implements QuestionChapterTreeService {

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Resource
    private IQuestionSectionService iQuestionSectionService;

    @Override
    public List<AppQuestionChapterNodeVO> getChapterTree(AppQuestionChapterTreeQueryBO query) {
        String grade = SetupQueryResolveSupport.resolveGradeValue(query.getGrade());
        String subject = SetupQueryResolveSupport.resolveSubjectValue(query.getSubject());
        String version = normalizeVersion(SetupQueryResolveSupport.resolveVersionValue(query.getVersion()));

        QueryWrapper<QuestionChapter> chapterQuery = new QueryWrapper<>();
        chapterQuery.lambda()
                .eq(QuestionChapter::getGrade, grade)
                .eq(QuestionChapter::getSubject, subject)
                .eq(QuestionChapter::getVersion, version)
                .eq(QuestionChapter::getState, QuestionChapterConstant.STATE_ENABLED)
                .orderByAsc(QuestionChapter::getSortOrder);

        List<QuestionChapter> chapters = iQuestionChapterService.list(chapterQuery);
        if (CollectionUtils.isEmpty(chapters)) {
            return Collections.emptyList();
        }

        List<Long> chapterIds = chapters.stream()
                .map(QuestionChapter::getId)
                .collect(Collectors.toList());

        QueryWrapper<QuestionSection> sectionQuery = new QueryWrapper<>();
        sectionQuery.lambda()
                .in(QuestionSection::getChapterId, chapterIds)
                .eq(QuestionSection::getState, QuestionChapterConstant.STATE_ENABLED)
                .orderByAsc(QuestionSection::getSortOrder);

        List<QuestionSection> sections = iQuestionSectionService.list(sectionQuery);
        Map<Long, List<QuestionSection>> sectionsByChapterId = CollectionUtils.isEmpty(sections)
                ? Collections.emptyMap()
                : sections.stream().collect(Collectors.groupingBy(QuestionSection::getChapterId));

        List<AppQuestionChapterNodeVO> result = new ArrayList<>(chapters.size());
        for (QuestionChapter chapter : chapters) {
            AppQuestionChapterNodeVO chapterNode = new AppQuestionChapterNodeVO();
            chapterNode.setCode(chapter.getChapterCode());
            chapterNode.setTitle(chapter.getTitle());
            chapterNode.setSort(chapter.getSortOrder());
            chapterNode.setQuestionCount(chapter.getQuestionCount());
            chapterNode.setGradeShort(chapter.getGradeShort());
            chapterNode.setSections(toSectionNodes(sectionsByChapterId.get(chapter.getId())));
            result.add(chapterNode);
        }
        return result;
    }

    private List<AppQuestionSectionNodeVO> toSectionNodes(List<QuestionSection> sections) {
        if (CollectionUtils.isEmpty(sections)) {
            return Collections.emptyList();
        }
        List<AppQuestionSectionNodeVO> sectionNodes = new ArrayList<>(sections.size());
        for (QuestionSection section : sections) {
            AppQuestionSectionNodeVO sectionNode = new AppQuestionSectionNodeVO();
            sectionNode.setCode(section.getSectionCode());
            sectionNode.setTitle(section.getTitle());
            sectionNode.setSort(section.getSortOrder());
            sectionNode.setQuestionCount(section.getQuestionCount());
            sectionNodes.add(sectionNode);
        }
        return sectionNodes;
    }

    private String normalizeVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        return version.replace("（默认）", "").trim();
    }
}
