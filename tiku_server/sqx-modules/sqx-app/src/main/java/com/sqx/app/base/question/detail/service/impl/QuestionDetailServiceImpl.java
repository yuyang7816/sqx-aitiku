package com.sqx.app.base.question.detail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.detail.service.QuestionDetailService;
import com.sqx.app.base.question.list.constants.QuestionAnswerStatus;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.app.base.question.list.support.QuestionTagsSupport;
import com.sqx.app.base.question.paper.support.QuestionOptionsSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户端单题详情服务实现
 */
@Service
public class QuestionDetailServiceImpl implements QuestionDetailService {

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @Override
    public AppQuestionListItemVO getQuestionDetail(Long userId, String id) {
        if (StringUtils.isBlank(id)) {
            throw new BizException("题目 id 不能为空");
        }
        Question question = resolveQuestionToken(id);
        if (question == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        UserQuestionAnswer answer = loadAnswer(userId, question.getId());
        return toDetailVO(question, answer);
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

    UserQuestionAnswer loadAnswer(Long userId, Long questionId) {
        if (userId == null || questionId == null) {
            return null;
        }
        LambdaQueryWrapper<UserQuestionAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionAnswer::getUserId, userId)
                .eq(UserQuestionAnswer::getQuestionId, questionId)
                .last("LIMIT 1");
        return iUserQuestionAnswerService.getOne(wrapper);
    }

    AppQuestionListItemVO toDetailVO(Question question, UserQuestionAnswer answer) {
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
}
