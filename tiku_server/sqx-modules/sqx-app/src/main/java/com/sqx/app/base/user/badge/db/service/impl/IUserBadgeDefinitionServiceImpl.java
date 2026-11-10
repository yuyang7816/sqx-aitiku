package com.sqx.app.base.user.badge.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.user.badge.db.mapper.UserBadgeDefinitionMapper;
import com.sqx.app.base.user.badge.db.service.IUserBadgeDefinitionService;
import com.sqx.app.base.user.badge.domain.entity.UserBadgeDefinition;
import org.springframework.stereotype.Service;

@Service
public class IUserBadgeDefinitionServiceImpl
        extends ServiceImpl<UserBadgeDefinitionMapper, UserBadgeDefinition>
        implements IUserBadgeDefinitionService {
}
