package com.sqx.app.base.question.chapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.question.chapter.domain.bo.*;
import com.sqx.app.base.question.chapter.domain.vo.QuestionSectionVo;

/**
 * 管理端节维护
 */
public interface QuestionSectionAdminService {

    void saveInfo(QuestionSectionSaveBo saveBo);

    IPage<QuestionSectionVo> pageInfo(QuestionSectionPageBo pageBo);

    QuestionSectionVo info(QuestionSectionInfoBo infoBo);

    void updateInfo(QuestionSectionUpdateBo updateBo);

    void delete(QuestionSectionInfoBo infoBo);

    void updateState(QuestionSectionUpdateStateBo updateStateBo);
}
