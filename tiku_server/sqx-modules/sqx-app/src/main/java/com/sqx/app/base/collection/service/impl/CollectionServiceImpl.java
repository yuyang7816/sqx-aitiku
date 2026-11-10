package com.sqx.app.base.collection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.collection.domain.bo.AppCollectionCreateBO;
import com.sqx.app.base.collection.domain.bo.AppCollectionQueryBO;
import com.sqx.app.base.collection.domain.vo.AppCollectionItemVO;
import com.sqx.app.base.collection.domain.vo.AppCollectionToggleResultVO;
import com.sqx.app.base.collection.service.CollectionService;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.list.support.QuestionTagsSupport;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import com.sqx.app.base.question.paper.support.QuestionOptionsSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.entity.UserQuestionCollection;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import com.sqx.system.db.service.IUserQuestionCollectionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户端题目收藏：基于 tb_user_question_collection，按 userId + questionId 去重
 */
@Service
public class CollectionServiceImpl implements CollectionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DEFAULT_AI_SUMMARY = "暂无 AI 讲解";
    private static final String DEFAULT_TAG = "#已收藏";

    @Resource
    private IUserQuestionCollectionService iUserQuestionCollectionService;

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @Override
    public List<AppCollectionItemVO> listCollections(Long userId, AppCollectionQueryBO query) {
        LambdaQueryWrapper<UserQuestionCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionCollection::getUserId, userId);
        if (query != null && StringUtils.isNotBlank(query.getSubject())) {
            wrapper.eq(UserQuestionCollection::getSubject, StringUtils.trim(query.getSubject()));
        }
        wrapper.orderByDesc(UserQuestionCollection::getCreateTime);

        List<UserQuestionCollection> collections = iUserQuestionCollectionService.list(wrapper);
        if (collections.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = loadQuestionMap(collections);
        Map<Long, UserQuestionAnswer> answerMap = loadAnswerMap(userId, questionMap);

        List<AppCollectionItemVO> result = new ArrayList<>();
        for (UserQuestionCollection collection : collections) {
            Question question = questionMap.get(collection.getQuestionId());
            if (question == null) {
                continue;
            }
            result.add(toItemVO(collection, question, answerMap.get(question.getId())));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppCollectionToggleResultVO addCollection(Long userId, AppCollectionCreateBO create) {
        Question question = resolveQuestionToken(create.getQuestionId());
        if (question == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        String subject = StringUtils.trim(create.getSubject());
        UserQuestionCollection collection = findCollection(userId, question.getId());
        if (collection == null) {
            collection = new UserQuestionCollection();
            collection.setUserId(userId);
            collection.setQuestionId(question.getId());
            collection.setSubject(subject);
            iUserQuestionCollectionService.save(collection);
        } else if (!StringUtils.equals(collection.getSubject(), subject)) {
            collection.setSubject(subject);
            iUserQuestionCollectionService.updateById(collection);
        }

        UserQuestionAnswer answer = loadAnswer(userId, question.getId());
        AppCollectionToggleResultVO result = new AppCollectionToggleResultVO();
        result.setCollected(Boolean.TRUE);
        result.setItem(toItemVO(collection, question, answer));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCollection(Long userId, String questionId) {
        Question question = resolveQuestionToken(questionId);
        if (question == null) {
            return;
        }
        LambdaQueryWrapper<UserQuestionCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionCollection::getUserId, userId)
                .eq(UserQuestionCollection::getQuestionId, question.getId());
        iUserQuestionCollectionService.remove(wrapper);
    }

    UserQuestionCollection findCollection(Long userId, Long questionId) {
        LambdaQueryWrapper<UserQuestionCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionCollection::getUserId, userId)
                .eq(UserQuestionCollection::getQuestionId, questionId)
                .last("LIMIT 1");
        return iUserQuestionCollectionService.getOne(wrapper);
    }

    Map<Long, Question> loadQuestionMap(List<UserQuestionCollection> collections) {
        List<Long> questionIds = collections.stream()
                .map(UserQuestionCollection::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iQuestionService.listByIds(questionIds).stream()
                .filter(this::isEnabledQuestion)
                .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    Map<Long, UserQuestionAnswer> loadAnswerMap(Long userId, Map<Long, Question> questionMap) {
        if (userId == null || questionMap.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> questionIds = new ArrayList<>(questionMap.keySet());
        LambdaQueryWrapper<UserQuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionAnswer::getUserId, userId)
                .in(UserQuestionAnswer::getQuestionId, questionIds);
        return iUserQuestionAnswerService.list(wrapper).stream()
                .collect(Collectors.toMap(UserQuestionAnswer::getQuestionId, Function.identity(), (a, b) -> a));
    }

    UserQuestionAnswer loadAnswer(Long userId, Long questionId) {
        LambdaQueryWrapper<UserQuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionAnswer::getUserId, userId)
                .eq(UserQuestionAnswer::getQuestionId, questionId)
                .last("LIMIT 1");
        return iUserQuestionAnswerService.getOne(wrapper);
    }

    Question resolveQuestionToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String value = StringUtils.trim(token);
        if (StringUtils.isNumeric(value)) {
            Question byId = iQuestionService.getById(Long.valueOf(value));
            return isEnabledQuestion(byId) ? byId : null;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuestionCode, value)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionService.getOne(wrapper);
    }

    boolean isEnabledQuestion(Question question) {
        return question != null
                && Integer.valueOf(QuestionChapterConstant.STATE_ENABLED).equals(question.getState());
    }

    AppCollectionItemVO toItemVO(UserQuestionCollection collection, Question question, UserQuestionAnswer answer) {
        AppCollectionItemVO vo = new AppCollectionItemVO();
        vo.setId(question.getQuestionCode());
        vo.setCollectionId(collection.getId());
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setSubject(StringUtils.defaultIfBlank(collection.getSubject(), "通用题目"));
        if (collection.getCreateTime() != null) {
            vo.setDate(collection.getCreateTime().format(DATE_FORMATTER));
        }
        vo.setTitle(StringUtils.defaultString(question.getTitle()));
        vo.setMyAnswer(answer == null ? "" : StringUtils.defaultString(answer.getUserAnswer()));
        vo.setRightAnswer(StringUtils.defaultString(question.getRightAnswer()));
        vo.setAnalysis(StringUtils.defaultIfBlank(question.getAnalysis(), "暂无解析"));
        vo.setAiSummary(DEFAULT_AI_SUMMARY);
        List<AppQuestionOptionVO> options = QuestionOptionsSupport.parseOptions(question.getOptionsJson());
        vo.setOptions(options);
        vo.setTags(resolveTags(question.getTagsJson()));
        return vo;
    }

    List<String> resolveTags(String tagsJson) {
        List<String> tags = QuestionTagsSupport.parseTags(tagsJson);
        if (tags.isEmpty()) {
            return Collections.singletonList(DEFAULT_TAG);
        }
        return tags;
    }
}
