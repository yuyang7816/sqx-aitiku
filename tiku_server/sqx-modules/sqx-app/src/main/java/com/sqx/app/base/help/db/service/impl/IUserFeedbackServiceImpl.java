package com.sqx.app.base.help.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.help.db.mapper.UserFeedbackMapper;
import com.sqx.app.base.help.db.service.IUserFeedbackService;
import com.sqx.app.base.help.domain.entity.UserFeedback;
import org.springframework.stereotype.Service;

@Service
public class IUserFeedbackServiceImpl extends ServiceImpl<UserFeedbackMapper, UserFeedback> implements IUserFeedbackService {
}
