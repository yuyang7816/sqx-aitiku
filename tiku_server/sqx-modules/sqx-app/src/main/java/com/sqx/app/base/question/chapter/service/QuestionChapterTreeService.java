package com.sqx.app.base.question.chapter.service;

import com.sqx.app.base.question.chapter.domain.bo.AppQuestionChapterTreeQueryBO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionChapterNodeVO;

import java.util.List;

/**
 * 用户端题库章节树服务
 */
public interface QuestionChapterTreeService {

    /**
     * 按年级、学科、教材版本查询启用中的章节树
     */
    List<AppQuestionChapterNodeVO> getChapterTree(AppQuestionChapterTreeQueryBO query);
}
