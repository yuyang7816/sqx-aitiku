package com.sqx.app.base.question.list.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.question.list.domain.bo.AppQuestionListQueryBO;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;

/**
 * 用户端章节题目列表服务
 */
public interface QuestionListService {

    /**
     * 分页查询练习题目，含完整详情与当前用户做题状态
     *
     * @param userId 当前登录用户 id
     * @param query  查询参数（chapterId / paperId / questionIds、mode、page、size）
     * @return 分页题目列表
     */
    IPage<AppQuestionListItemVO> pageQuestionList(Long userId, AppQuestionListQueryBO query);
}
