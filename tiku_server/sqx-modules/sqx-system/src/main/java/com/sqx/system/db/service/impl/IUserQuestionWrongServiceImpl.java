package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.mapper.UserQuestionWrongMapper;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.springframework.stereotype.Service;

/**
 * 用户错题本 Service 实现
 *
 * @author sqx
 */
@Service
public class IUserQuestionWrongServiceImpl extends ServiceImpl<UserQuestionWrongMapper, UserQuestionWrong>
        implements IUserQuestionWrongService {
}
