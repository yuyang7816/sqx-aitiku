package com.sqx.app.base.question.chapter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.question.chapter.domain.bo.*;
import com.sqx.app.base.question.chapter.domain.vo.QuestionChapterVo;

/**
 * 管理端章维护
 */
public interface QuestionChapterAdminService {

    void saveInfo(QuestionChapterSaveBo saveBo);

    IPage<QuestionChapterVo> pageInfo(QuestionChapterPageBo pageBo);

    QuestionChapterVo info(QuestionChapterInfoBo infoBo);

    void updateInfo(QuestionChapterUpdateBo updateBo);

    void delete(QuestionChapterInfoBo infoBo);

    void updateState(QuestionChapterUpdateStateBo updateStateBo);
}
