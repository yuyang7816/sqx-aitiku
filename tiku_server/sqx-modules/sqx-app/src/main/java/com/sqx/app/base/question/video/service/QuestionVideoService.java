package com.sqx.app.base.question.video.service;

import com.sqx.app.base.question.video.domain.vo.AppQuestionVideoVO;

/**
 * 题目讲解视频
 */
public interface QuestionVideoService {

    /**
     * 按 questionCode 或 questionId 返回讲解视频
     */
    AppQuestionVideoVO getQuestionVideo(String questionCode, Long questionId);
}
