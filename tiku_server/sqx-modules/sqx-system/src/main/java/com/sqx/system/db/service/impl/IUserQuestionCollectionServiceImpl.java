package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserQuestionCollection;
import com.sqx.system.db.mapper.UserQuestionCollectionMapper;
import com.sqx.system.db.service.IUserQuestionCollectionService;
import org.springframework.stereotype.Service;

/**
 * 用户题目收藏 服务实现类
 *
 * @author sqx
 */
@Service
public class IUserQuestionCollectionServiceImpl extends ServiceImpl<UserQuestionCollectionMapper, UserQuestionCollection>
        implements IUserQuestionCollectionService {
}
