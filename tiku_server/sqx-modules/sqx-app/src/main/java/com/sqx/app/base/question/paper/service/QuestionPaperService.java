package com.sqx.app.base.question.paper.service;

import com.sqx.app.base.question.paper.domain.bo.AppQuestionPaperListQueryBO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperItemVO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperQuestionVO;

import java.util.List;

/**
 * 用户端专项/真题题集服务
 */
public interface QuestionPaperService {

    /**
     * 按类型返回启用中的题集列表
     */
    List<AppQuestionPaperItemVO> listPapers(AppQuestionPaperListQueryBO query);

    /**
     * 按题集业务编码返回组卷题目（含选项与答案）
     */
    List<AppQuestionPaperQuestionVO> listPaperQuestions(String paperId);
}
