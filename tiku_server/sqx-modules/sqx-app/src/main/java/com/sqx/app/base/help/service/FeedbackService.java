package com.sqx.app.base.help.service;

import com.sqx.app.base.help.domain.bo.FeedbackSubmitBo;
import com.sqx.app.base.help.domain.vo.AppFeedbackSubmitResultVO;

public interface FeedbackService {

    AppFeedbackSubmitResultVO submit(Long userId, FeedbackSubmitBo submitBo);
}
