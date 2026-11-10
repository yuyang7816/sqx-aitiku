package com.sqx.app.base.question.paper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.question.paper.domain.bo.*;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperItemVo;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperQuestionOptionVo;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperVo;

/**
 * 管理端题集维护
 */
public interface QuestionPaperAdminService {

    void saveInfo(QuestionPaperSaveBo saveBo);

    IPage<QuestionPaperVo> pageInfo(QuestionPaperPageBo pageBo);

    QuestionPaperVo info(QuestionPaperInfoBo infoBo);

    void updateInfo(QuestionPaperUpdateBo updateBo);

    void delete(QuestionPaperInfoBo infoBo);

    void updateState(QuestionPaperUpdateStateBo updateStateBo);

    IPage<QuestionPaperQuestionOptionVo> searchQuestions(QuestionPaperQuestionSearchBo searchBo);

    IPage<QuestionPaperItemVo> pageItems(QuestionPaperItemPageBo pageBo);

    void bindQuestions(QuestionPaperBindBo bindBo);

    void unbindQuestion(QuestionPaperUnbindBo unbindBo);
}
