package com.sqx.app.base.help.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.help.domain.bo.FeedbackInfoBo;
import com.sqx.app.base.help.domain.bo.FeedbackPageBo;
import com.sqx.app.base.help.domain.bo.FeedbackUpdateBo;
import com.sqx.app.base.help.domain.vo.FeedbackVo;

public interface AdminFeedbackService {

    IPage<FeedbackVo> pageInfo(FeedbackPageBo pageBo);

    FeedbackVo info(FeedbackInfoBo infoBo);

    void updateInfo(FeedbackUpdateBo updateBo);
}
