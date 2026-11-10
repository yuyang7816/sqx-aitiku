package com.sqx.app.base.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.search.domain.bo.AppQuestionSearchQueryBO;
import com.sqx.app.base.search.domain.vo.AppQuestionSearchItemVO;
import com.sqx.app.base.search.service.QuestionSearchService;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 题库关键词搜索：题干 LIKE 匹配，仅返回启用题目
 */
@Service
public class QuestionSearchServiceImpl implements QuestionSearchService {

    private static final int MAX_PAGE_SIZE = 20;

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Override
    public IPage<AppQuestionSearchItemVO> searchQuestions(AppQuestionSearchQueryBO query) {
        String keyword = StringUtils.trim(query.getKeyword());
        Page<Question> page = PageUtil.initPage(
                query.getPage() == null ? 1L : query.getPage().longValue(),
                (long) clampSize(query.getSize()));

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .like(Question::getTitle, keyword)
                .orderByAsc(Question::getSortOrder)
                .orderByAsc(Question::getId);

        IPage<Question> questionPage = iQuestionService.page(page, wrapper);
        List<Question> records = questionPage.getRecords();
        Map<Long, QuestionChapter> chapterMap = loadChapterMap(records);

        Page<AppQuestionSearchItemVO> result = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        result.setRecords(records.stream()
                .map(item -> toItemVO(item, chapterMap.get(item.getChapterId())))
                .collect(Collectors.toList()));
        return result;
    }

    int clampSize(Integer size) {
        int value = size == null ? 10 : size;
        return Math.max(1, Math.min(MAX_PAGE_SIZE, value));
    }

    Map<Long, QuestionChapter> loadChapterMap(List<Question> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> chapterIds = records.stream()
                .map(Question::getChapterId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (chapterIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iQuestionChapterService.listByIds(chapterIds).stream()
                .collect(Collectors.toMap(QuestionChapter::getId, chapter -> chapter, (a, b) -> a));
    }

    AppQuestionSearchItemVO toItemVO(Question question, QuestionChapter chapter) {
        AppQuestionSearchItemVO vo = new AppQuestionSearchItemVO();
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setTitle(question.getTitle());
        vo.setQuestionType(question.getQuestionType());
        vo.setDifficulty(question.getDifficulty());
        if (chapter != null) {
            vo.setSubject(chapter.getSubject());
            vo.setChapterTitle(chapter.getTitle());
        }
        return vo;
    }
}
