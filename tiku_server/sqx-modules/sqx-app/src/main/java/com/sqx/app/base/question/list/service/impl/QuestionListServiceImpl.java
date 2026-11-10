package com.sqx.app.base.question.list.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.list.constants.QuestionAnswerStatus;
import com.sqx.app.base.question.list.constants.QuestionListMode;
import com.sqx.app.base.question.list.domain.bo.AppQuestionListQueryBO;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.app.base.question.list.service.QuestionListService;
import com.sqx.app.base.question.list.support.QuestionTagsSupport;
import com.sqx.app.base.question.paper.constants.QuestionPaperConstant;
import com.sqx.app.base.question.paper.support.QuestionOptionsSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户端练习题目拉取服务实现
 */
@Service
public class QuestionListServiceImpl implements QuestionListService {

    @Resource
    private IQuestionChapterService iQuestionChapterService;

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @Resource
    private IQuestionPaperService iQuestionPaperService;

    @Resource
    private IQuestionPaperItemService iQuestionPaperItemService;

    @Resource
    private IKnowledgePointService iKnowledgePointService;

    @Resource
    private IKnowledgePointQuestionService iKnowledgePointQuestionService;

    @Override
    public IPage<AppQuestionListItemVO> pageQuestionList(Long userId, AppQuestionListQueryBO query) {
        validateQuery(query);
        String mode = resolveMode(query.getMode());

        if (StringUtils.isNotBlank(query.getPaperId())) {
            return pageResolvedQuestions(userId, loadQuestionsByPaper(query.getPaperId()), mode, query);
        }
        if (StringUtils.isNotBlank(query.getKnowledgePoint())) {
            return pageResolvedQuestions(userId, loadQuestionsByKnowledgePoint(query.getKnowledgePoint()), mode, query);
        }
        if (StringUtils.isNotBlank(query.getQuestionIds())) {
            return pageResolvedQuestions(userId, loadQuestionsByIds(query.getQuestionIds()), mode, query);
        }
        return pageChapterQuestions(userId, query, mode);
    }

    void validateQuery(AppQuestionListQueryBO query) {
        boolean hasSource = StringUtils.isNotBlank(query.getChapterId())
                || StringUtils.isNotBlank(query.getPaperId())
                || StringUtils.isNotBlank(query.getKnowledgePoint())
                || StringUtils.isNotBlank(query.getQuestionIds());
        if (!hasSource) {
            throw new BizException("chapterId、paperId、knowledgePoint、questionIds 至少传一项");
        }
        if (StringUtils.isNotBlank(query.getMode())) {
            String mode = StringUtils.trim(query.getMode());
            if (!QuestionListMode.VALID_MODES.contains(mode)) {
                throw new BizException("mode 仅支持 all、wrong、single");
            }
        }
        if (QuestionListMode.SINGLE.equals(resolveMode(query.getMode()))
                && StringUtils.isBlank(query.getQuestionIds())
                && StringUtils.isBlank(query.getPaperId())) {
            throw new BizException("mode=single 时需传 questionIds");
        }
    }

    String resolveMode(String mode) {
        String value = StringUtils.trim(mode);
        return StringUtils.isBlank(value) ? QuestionListMode.ALL : value;
    }

    IPage<AppQuestionListItemVO> pageChapterQuestions(Long userId, AppQuestionListQueryBO query, String mode) {
        QuestionChapter chapter = resolveChapter(query.getChapterId());
        Page<Question> initPage = PageUtil.initPage(query.getPage(), query.getSize());
        if (chapter == null) {
            return emptyPage(initPage);
        }

        if (QuestionListMode.WRONG.equals(mode)) {
            List<Question> chapterQuestions = listChapterQuestions(chapter.getId());
            List<Question> wrongQuestions = filterWrongQuestions(userId, chapterQuestions);
            return pageInMemory(userId, wrongQuestions, initPage);
        }

        LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(Question::getChapterId, chapter.getId())
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .orderByAsc(Question::getSortOrder)
                .orderByAsc(Question::getId);

        IPage<Question> questionPage = iQuestionService.page(initPage, questionWrapper);
        return buildResultPage(userId, questionPage, questionPage.getRecords());
    }

    IPage<AppQuestionListItemVO> pageResolvedQuestions(Long userId, List<Question> questions, String mode,
                                                       AppQuestionListQueryBO query) {
        Page<Question> initPage = PageUtil.initPage(query.getPage(), query.getSize());
        if (questions.isEmpty()) {
            return emptyPage(initPage);
        }
        List<Question> filtered = filterByMode(userId, questions, mode);
        return pageInMemory(userId, filtered, initPage);
    }

    List<Question> loadQuestionsByKnowledgePoint(String knowledgePoint) {
        KnowledgePoint point = resolveKnowledgePoint(knowledgePoint);
        if (point == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<KnowledgePointQuestion> linkWrapper = new LambdaQueryWrapper<>();
        linkWrapper.eq(KnowledgePointQuestion::getPointId, point.getId())
                .orderByAsc(KnowledgePointQuestion::getSortOrder)
                .orderByAsc(KnowledgePointQuestion::getId);
        List<KnowledgePointQuestion> links = iKnowledgePointQuestionService.list(linkWrapper);
        if (links.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Question> questionMap = loadEnabledQuestionMapByIds(
                links.stream().map(KnowledgePointQuestion::getQuestionId).collect(Collectors.toList()));
        List<Question> questions = new ArrayList<>();
        for (KnowledgePointQuestion link : links) {
            Question question = questionMap.get(link.getQuestionId());
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    KnowledgePoint resolveKnowledgePoint(String knowledgePoint) {
        if (StringUtils.isBlank(knowledgePoint)) {
            return null;
        }
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getPointCode, StringUtils.trim(knowledgePoint))
                .eq(KnowledgePoint::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iKnowledgePointService.getOne(wrapper);
    }

    Map<Long, Question> loadEnabledQuestionMapByIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Question::getId, questionIds)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        return iQuestionService.list(wrapper).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));
    }

    List<Question> loadQuestionsByPaper(String paperId) {
        QuestionPaper paper = resolvePaper(paperId);
        if (paper == null) {
            return Collections.emptyList();
        }
        List<QuestionPaperItem> items = listPaperItems(paper.getId());
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Question> questionMap = loadEnabledQuestionMap(items);
        List<Question> questions = new ArrayList<>();
        for (QuestionPaperItem item : items) {
            Question question = questionMap.get(item.getQuestionId());
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    List<Question> loadQuestionsByIds(String questionIds) {
        List<String> tokens = parseQuestionIdTokens(questionIds);
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Question> ordered = new LinkedHashMap<>();
        for (String token : tokens) {
            Question question = resolveQuestionToken(token);
            if (question != null) {
                ordered.putIfAbsent(String.valueOf(question.getId()), question);
            }
        }
        return new ArrayList<>(ordered.values());
    }

    List<String> parseQuestionIdTokens(String questionIds) {
        if (StringUtils.isBlank(questionIds)) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(StringUtils.split(questionIds, ','))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    Question resolveQuestionToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (StringUtils.isNumeric(token)) {
            Question byId = iQuestionService.getById(Long.valueOf(token));
            return isEnabledQuestion(byId) ? byId : null;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuestionCode, token)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionService.getOne(wrapper);
    }

    List<Question> listChapterQuestions(Long chapterId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getChapterId, chapterId)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .orderByAsc(Question::getSortOrder)
                .orderByAsc(Question::getId);
        return iQuestionService.list(wrapper);
    }

    List<Question> filterByMode(Long userId, List<Question> questions, String mode) {
        if (QuestionListMode.WRONG.equals(mode)) {
            return filterWrongQuestions(userId, questions);
        }
        return questions;
    }

    List<Question> filterWrongQuestions(Long userId, List<Question> questions) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, UserQuestionAnswer> answerMap = loadAnswerMap(userId, questions);
        return questions.stream()
                .filter(question -> {
                    UserQuestionAnswer answer = answerMap.get(question.getId());
                    return answer != null
                            && Integer.valueOf(QuestionAnswerStatus.CORRECT_NO).equals(answer.getIsCorrect());
                })
                .collect(Collectors.toList());
    }

    IPage<AppQuestionListItemVO> pageInMemory(Long userId, List<Question> questions, Page<Question> initPage) {
        long current = initPage.getCurrent();
        long size = initPage.getSize();
        long total = questions.size();
        long fromIndex = Math.max((current - 1) * size, 0);
        if (fromIndex >= total) {
            IPage<AppQuestionListItemVO> page = new Page<>();
            page.setCurrent(current);
            page.setSize(size);
            page.setTotal(total);
            page.setPages(total == 0 ? 0 : (total + size - 1) / size);
            page.setRecords(Collections.emptyList());
            return page;
        }
        int from = (int) fromIndex;
        int to = (int) Math.min(fromIndex + size, total);
        List<Question> pageRecords = questions.subList(from, to);

        IPage<Question> questionPage = new Page<>();
        questionPage.setCurrent(current);
        questionPage.setSize(size);
        questionPage.setTotal(total);
        questionPage.setPages(total == 0 ? 0 : (total + size - 1) / size);
        questionPage.setRecords(pageRecords);
        return buildResultPage(userId, questionPage, pageRecords);
    }

    IPage<AppQuestionListItemVO> buildResultPage(Long userId, IPage<Question> questionPage, List<Question> questions) {
        if (questions.isEmpty()) {
            return PageUtil.copyPage(questionPage, Collections.emptyList());
        }
        Map<Long, UserQuestionAnswer> answerMap = loadAnswerMap(userId, questions);
        List<AppQuestionListItemVO> records = questions.stream()
                .map(question -> toListItemVO(question, answerMap.get(question.getId())))
                .collect(Collectors.toList());
        return PageUtil.copyPage(questionPage, records);
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

    QuestionPaper resolvePaper(String paperId) {
        if (StringUtils.isBlank(paperId)) {
            return null;
        }
        LambdaQueryWrapper<QuestionPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaper::getPaperCode, StringUtils.trim(paperId))
                .eq(QuestionPaper::getState, QuestionPaperConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionPaperService.getOne(wrapper);
    }

    List<QuestionPaperItem> listPaperItems(Long paperId) {
        LambdaQueryWrapper<QuestionPaperItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionPaperItem::getPaperId, paperId)
                .orderByAsc(QuestionPaperItem::getSortOrder)
                .orderByAsc(QuestionPaperItem::getId);
        return iQuestionPaperItemService.list(wrapper);
    }

    Map<Long, Question> loadEnabledQuestionMap(List<QuestionPaperItem> items) {
        List<Long> questionIds = items.stream()
                .map(QuestionPaperItem::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Question::getId, questionIds)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED);
        return iQuestionService.list(wrapper).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));
    }

    boolean isEnabledQuestion(Question question) {
        return question != null
                && Integer.valueOf(QuestionChapterConstant.STATE_ENABLED).equals(question.getState());
    }

    Map<Long, UserQuestionAnswer> loadAnswerMap(Long userId, List<Question> questions) {
        if (userId == null || questions.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        LambdaQueryWrapper<UserQuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionAnswer::getUserId, userId)
                .in(UserQuestionAnswer::getQuestionId, questionIds);
        return iUserQuestionAnswerService.list(wrapper).stream()
                .collect(Collectors.toMap(UserQuestionAnswer::getQuestionId, Function.identity(), (a, b) -> a));
    }

    AppQuestionListItemVO toListItemVO(Question question, UserQuestionAnswer answer) {
        AppQuestionListItemVO vo = new AppQuestionListItemVO();
        vo.setNo(formatNo(question.getSortOrder()));
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setText(question.getTitle());
        vo.setTitle(question.getTitle());
        vo.setQuestionType(StringUtils.defaultIfBlank(question.getQuestionType(), "单选题"));
        vo.setDifficulty(StringUtils.defaultIfBlank(question.getDifficulty(), "中等"));
        vo.setRightAnswer(StringUtils.defaultString(question.getRightAnswer()));
        vo.setAnalysis(StringUtils.defaultString(question.getAnalysis()));
        vo.setOptions(QuestionOptionsSupport.parseOptions(question.getOptionsJson()));
        vo.setTags(QuestionTagsSupport.parseTags(question.getTagsJson()));
        applyAnswerStatus(vo, answer);
        return vo;
    }

    void applyAnswerStatus(AppQuestionListItemVO vo, UserQuestionAnswer answer) {
        if (answer == null) {
            vo.setType(QuestionAnswerStatus.TYPE_TODO);
            vo.setStatus(QuestionAnswerStatus.LABEL_TODO);
            return;
        }
        if (Integer.valueOf(QuestionAnswerStatus.CORRECT_YES).equals(answer.getIsCorrect())) {
            vo.setType(QuestionAnswerStatus.TYPE_RIGHT);
            vo.setStatus(QuestionAnswerStatus.LABEL_RIGHT);
            return;
        }
        vo.setType(QuestionAnswerStatus.TYPE_WRONG);
        vo.setStatus(QuestionAnswerStatus.LABEL_WRONG);
    }

    String formatNo(Integer sortOrder) {
        int value = sortOrder == null || sortOrder <= 0 ? 0 : sortOrder;
        return String.format("%02d", value);
    }

    IPage<AppQuestionListItemVO> emptyPage(Page<Question> initPage) {
        IPage<AppQuestionListItemVO> page = new Page<>();
        page.setCurrent(initPage.getCurrent());
        page.setSize(initPage.getSize());
        page.setTotal(0);
        page.setPages(0);
        page.setRecords(Collections.emptyList());
        return page;
    }
}
