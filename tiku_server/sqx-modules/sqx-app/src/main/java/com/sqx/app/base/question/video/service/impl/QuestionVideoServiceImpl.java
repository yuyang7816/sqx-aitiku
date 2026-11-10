package com.sqx.app.base.question.video.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.question.chapter.constants.QuestionChapterConstant;
import com.sqx.app.base.question.video.domain.vo.AppQuestionVideoVO;
import com.sqx.app.base.question.video.service.QuestionVideoService;
import com.sqx.app.base.question.video.support.QuestionVideoCatalogSupport;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.service.IQuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 题目讲解视频：优先题目表字段，其次 sys_config 目录，最后默认配置
 */
@Service
public class QuestionVideoServiceImpl implements QuestionVideoService {

    @Resource
    private IQuestionService iQuestionService;

    @Override
    public AppQuestionVideoVO getQuestionVideo(String questionCode, Long questionId) {
        Question question = resolveQuestion(questionCode, questionId);
        if (question != null && StringUtils.isNotBlank(question.getVideoUrl())) {
            return buildFromQuestion(question);
        }

        String code = question != null ? question.getQuestionCode() : StringUtils.trimToNull(questionCode);
        String title = question != null ? question.getTitle() : "";

        JSONObject matched = QuestionVideoCatalogSupport.resolveCatalogEntry(code, title);
        AppQuestionVideoVO vo = new AppQuestionVideoVO();
        vo.setQuestionCode(code);
        vo.setTitle(StringUtils.defaultIfBlank(title, "题目视频讲解"));
        vo.setFromServer(Boolean.FALSE);

        if (matched != null) {
            vo.setVideoUrl(StringUtils.defaultIfBlank(matched.getString("videoUrl"),
                    QuestionVideoCatalogSupport.defaultVideoUrl()));
            vo.setPosterUrl(StringUtils.trimToNull(matched.getString("posterUrl")));
            vo.setDuration(StringUtils.defaultIfBlank(matched.getString("duration"),
                    QuestionVideoCatalogSupport.defaultDuration()));
            vo.setTeacher(StringUtils.defaultIfBlank(matched.getString("teacher"),
                    QuestionVideoCatalogSupport.defaultTeacher()));
            vo.setSummary(StringUtils.defaultIfBlank(matched.getString("summary"),
                    QuestionVideoCatalogSupport.defaultSummary()));
            vo.setFromServer(Boolean.TRUE);
            return vo;
        }

        vo.setVideoUrl(QuestionVideoCatalogSupport.defaultVideoUrl());
        vo.setPosterUrl(null);
        vo.setDuration(QuestionVideoCatalogSupport.defaultDuration());
        vo.setTeacher(QuestionVideoCatalogSupport.defaultTeacher());
        vo.setSummary(QuestionVideoCatalogSupport.defaultSummary());
        vo.setFromServer(Boolean.TRUE);
        return vo;
    }

    private AppQuestionVideoVO buildFromQuestion(Question question) {
        AppQuestionVideoVO vo = new AppQuestionVideoVO();
        vo.setQuestionCode(question.getQuestionCode());
        vo.setTitle(StringUtils.defaultIfBlank(question.getTitle(), "题目视频讲解"));
        vo.setVideoUrl(StringUtils.trim(question.getVideoUrl()));
        vo.setPosterUrl(StringUtils.trimToNull(question.getVideoPosterUrl()));
        vo.setDuration(StringUtils.defaultIfBlank(question.getVideoDuration(),
                QuestionVideoCatalogSupport.defaultDuration()));
        vo.setTeacher(StringUtils.defaultIfBlank(question.getVideoTeacher(),
                QuestionVideoCatalogSupport.defaultTeacher()));
        vo.setSummary(StringUtils.defaultIfBlank(question.getVideoSummary(),
                QuestionVideoCatalogSupport.defaultSummary()));
        vo.setFromServer(Boolean.TRUE);
        return vo;
    }

    Question resolveQuestion(String questionCode, Long questionId) {
        if (questionId != null) {
            Question byId = iQuestionService.getById(questionId);
            if (byId != null && isEnabled(byId)) {
                return byId;
            }
        }
        String code = StringUtils.trimToNull(questionCode);
        if (code == null) {
            return null;
        }
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getQuestionCode, code)
                .eq(Question::getState, QuestionChapterConstant.STATE_ENABLED)
                .last("LIMIT 1");
        return iQuestionService.getOne(wrapper);
    }

    boolean isEnabled(Question question) {
        return question != null
                && question.getState() != null
                && question.getState() == QuestionChapterConstant.STATE_ENABLED;
    }
}
