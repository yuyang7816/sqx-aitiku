package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.mapper.UserQuestionAnswerMapper;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import org.springframework.stereotype.Service;

/**
 * 用户答题记录 服务实现类
 *
 * @author sqx
 */
@Service
public class IUserQuestionAnswerServiceImpl extends ServiceImpl<UserQuestionAnswerMapper, UserQuestionAnswer> implements IUserQuestionAnswerService {
}
