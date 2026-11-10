package com.sqx.app.base.question.detail.service;

import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;

/**
 * 用户端单题详情（GET /app/question/{id}）
 */
public interface QuestionDetailService {

    /**
     * 按题目 id 或 question_code 返回单题详情及当前用户做题状态
     *
     * @param userId 当前用户
     * @param id     题目主键或业务编码
     */
    AppQuestionListItemVO getQuestionDetail(Long userId, String id);
}
