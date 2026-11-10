package com.sqx.app.base.search.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.search.domain.bo.AppQuestionSearchQueryBO;
import com.sqx.app.base.search.domain.vo.AppQuestionSearchItemVO;

/**
 * 题库关键词搜索
 */
public interface QuestionSearchService {

    /**
     * 按题干关键词分页检索启用中的题目
     */
    IPage<AppQuestionSearchItemVO> searchQuestions(AppQuestionSearchQueryBO query);
}
