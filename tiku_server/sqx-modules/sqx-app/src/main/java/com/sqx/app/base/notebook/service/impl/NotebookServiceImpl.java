package com.sqx.app.base.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.notebook.domain.bo.AppNotebookCreateBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookQueryBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookUpdateBO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookAddResultVO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.notebook.service.NotebookService;
import com.sqx.app.base.notebook.support.NotebookItemAssembler;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户端错题本：基于 tb_user_question_wrong，按 userId + questionId 去重
 */
@Service
public class NotebookServiceImpl implements NotebookService {

    @Resource
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private NotebookItemAssembler notebookItemAssembler;

    @Override
    public List<AppNotebookItemVO> listNotebook(Long userId, AppNotebookQueryBO query) {
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId);
        if (query != null && StringUtils.isNotBlank(query.getSubject())) {
            wrapper.eq(UserQuestionWrong::getSubject, StringUtils.trim(query.getSubject()));
        }
        if (query != null && query.getMastered() != null) {
            int masteredFlag = Boolean.TRUE.equals(query.getMastered())
                    ? UserQuestionWrong.MASTERED_YES
                    : UserQuestionWrong.MASTERED_NO;
            wrapper.eq(UserQuestionWrong::getMastered, masteredFlag);
        }
        wrapper.orderByDesc(UserQuestionWrong::getLastPracticeAt)
                .orderByDesc(UserQuestionWrong::getUpdateTime);

        List<UserQuestionWrong> wrongList = iUserQuestionWrongService.list(wrapper);
        if (wrongList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = notebookItemAssembler.loadQuestionMap(wrongList, true);
        Map<Long, String> chapterSubjectMap = notebookItemAssembler.loadChapterSubjectMap(questionMap.values());
        List<AppNotebookItemVO> result = new ArrayList<>();
        for (UserQuestionWrong wrong : wrongList) {
            Question question = questionMap.get(wrong.getQuestionId());
            if (question == null) {
                continue;
            }
            result.add(notebookItemAssembler.toItemVO(wrong, question, chapterSubjectMap));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppNotebookAddResultVO addNotebook(Long userId, AppNotebookCreateBO create) {
        Question question = resolveQuestionToken(create.getQuestionId());
        if (question == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        String subject = StringUtils.trim(create.getSubject());
        String userAnswer = StringUtils.trimToEmpty(create.getUserAnswer());
        LocalDateTime now = LocalDateTime.now();
        Map<Long, String> chapterSubjectMap = notebookItemAssembler.loadChapterSubjectMap(
                Collections.singletonList(question));
        if (notebookItemAssembler.isGenericSubject(subject)) {
            String chapterSubject = chapterSubjectMap.get(question.getChapterId());
            if (StringUtils.isNotBlank(chapterSubject)) {
                subject = chapterSubject;
            }
        } else {
            subject = notebookItemAssembler.normalizeSubjectLabel(subject);
        }

        UserQuestionWrong existing = findWrongEntry(userId, question.getId());
        boolean duplicate = existing != null;
        UserQuestionWrong wrong;
        if (existing == null) {
            wrong = new UserQuestionWrong();
            wrong.setUserId(userId);
            wrong.setQuestionId(question.getId());
            wrong.setSubject(subject);
            wrong.setUserAnswer(userAnswer);
            wrong.setWrongCount(1);
            wrong.setMastered(UserQuestionWrong.MASTERED_NO);
            wrong.setLastPracticeAt(now);
            iUserQuestionWrongService.save(wrong);
        } else {
            wrong = existing;
            wrong.setSubject(subject);
            if (StringUtils.isNotBlank(userAnswer)) {
                wrong.setUserAnswer(userAnswer);
            }
            wrong.setWrongCount(Math.max(1, wrong.getWrongCount() == null ? 1 : wrong.getWrongCount()) + 1);
            wrong.setMastered(UserQuestionWrong.MASTERED_NO);
            wrong.setLastPracticeAt(now);
            iUserQuestionWrongService.updateById(wrong);
        }

        AppNotebookAddResultVO result = new AppNotebookAddResultVO();
        result.setAdded(!duplicate);
        result.setDuplicate(duplicate);
        result.setItem(notebookItemAssembler.toItemVO(wrong, question, chapterSubjectMap));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeNotebook(Long userId, String questionId) {
        Question question = resolveQuestionToken(questionId);
        if (question == null) {
            return;
        }
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId)
                .eq(UserQuestionWrong::getQuestionId, question.getId());
        iUserQuestionWrongService.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearNotebook(Long userId) {
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId);
        iUserQuestionWrongService.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppNotebookItemVO updateMastered(Long userId, AppNotebookUpdateBO update) {
        Question question = resolveQuestionToken(update.getQuestionId());
        if (question == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        UserQuestionWrong existing = findWrongEntry(userId, question.getId());
        if (existing == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        int masteredFlag = Boolean.TRUE.equals(update.getMastered())
                ? UserQuestionWrong.MASTERED_YES
                : UserQuestionWrong.MASTERED_NO;
        existing.setMastered(masteredFlag);
        existing.setLastPracticeAt(now);
        iUserQuestionWrongService.updateById(existing);
        Map<Long, String> chapterSubjectMap = notebookItemAssembler.loadChapterSubjectMap(
                Collections.singletonList(question));
        return notebookItemAssembler.toItemVO(existing, question, chapterSubjectMap);
    }

    UserQuestionWrong findWrongEntry(Long userId, Long questionId) {
        LambdaQueryWrapper<UserQuestionWrong> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuestionWrong::getUserId, userId)
                .eq(UserQuestionWrong::getQuestionId, questionId)
                .last("LIMIT 1");
        return iUserQuestionWrongService.getOne(wrapper);
    }

    Question resolveQuestionToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String value = StringUtils.trim(token);
        if (StringUtils.isNumeric(value)) {
            Question byId = iQuestionService.getById(Long.valueOf(value));
            return notebookItemAssembler.isEnabledQuestion(byId) ? byId : null;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuestionCode, value)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionService.getOne(wrapper);
    }

}
