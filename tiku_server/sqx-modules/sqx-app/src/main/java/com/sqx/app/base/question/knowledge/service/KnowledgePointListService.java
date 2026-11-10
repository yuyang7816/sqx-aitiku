package com.sqx.app.base.question.knowledge.service;

import com.sqx.app.base.question.knowledge.domain.bo.AppQuestionKnowledgeListQueryBO;
import com.sqx.app.base.question.knowledge.domain.vo.AppQuestionKnowledgePointVO;

import java.util.List;

/**
 * 用户端知识点列表
 */
public interface KnowledgePointListService {

    /**
     * 按年级/学科筛选启用知识点；无匹配时返回空列表
     */
    List<AppQuestionKnowledgePointVO> listKnowledgePoints(AppQuestionKnowledgeListQueryBO query);
}
