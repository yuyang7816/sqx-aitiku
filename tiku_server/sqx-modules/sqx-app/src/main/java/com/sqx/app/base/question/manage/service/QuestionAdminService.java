package com.sqx.app.base.question.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.question.manage.domain.bo.*;
import com.sqx.app.base.question.manage.domain.vo.QuestionBindOptionVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionImportResultVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionVo;

import java.util.List;

/**
 * 管理端题目维护
 */
public interface QuestionAdminService {

    void saveInfo(QuestionSaveBo saveBo);

    IPage<QuestionVo> pageInfo(QuestionPageBo pageBo);

    QuestionVo info(QuestionInfoBo infoBo);

    void updateInfo(QuestionUpdateBo updateBo);

    void delete(QuestionInfoBo infoBo);

    void updateState(QuestionUpdateStateBo updateStateBo);

    QuestionImportResultVo importBatch(QuestionImportBo importBo);

    List<QuestionBindOptionVo> listKnowledgeOptions(QuestionBindOptionQueryBo queryBo);

    List<QuestionBindOptionVo> listPaperOptions(QuestionBindOptionQueryBo queryBo);
}
