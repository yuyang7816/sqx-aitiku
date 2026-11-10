package com.sqx.app.base.user.badge.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.user.badge.db.service.IUserBadgeDefinitionService;
import com.sqx.app.base.user.badge.domain.entity.UserBadgeDefinition;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserBadgeDefinitionProviderImpl implements UserBadgeDefinitionProvider {

    @Resource
    private IUserBadgeDefinitionService iUserBadgeDefinitionService;

    @Override
    public List<UserBadgeDefinition> listActiveDefinitions() {
        QueryWrapper<UserBadgeDefinition> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(UserBadgeDefinition::getState, UserBadgeDefinition.STATE_ENABLED)
                .orderByAsc(UserBadgeDefinition::getSortOrder)
                .orderByAsc(UserBadgeDefinition::getId);
        List<UserBadgeDefinition> records = iUserBadgeDefinitionService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return DefaultUserBadgeDefinitions.list();
        }
        return records;
    }
}
